package net.lordofthecraft.arche.persona;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import co.lotc.core.bukkit.util.WeakBlock;
import lombok.Getter;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.attributes.ModifierBuilder;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier.Decay;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.persona.PersonaFatigueEvent;
import net.lordofthecraft.arche.event.persona.PersonaRenameEvent;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.interfaces.Transaction;
import net.lordofthecraft.arche.listener.NewbieProtectListener;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.PersonaTable;
import net.lordofthecraft.arche.save.rows.persona.NamelogRow;
import net.lordofthecraft.arche.save.rows.persona.UpdatePersonaRow;
import net.lordofthecraft.arche.skin.ArcheSkin;

public final class ArchePersona extends ArcheOfflinePersona implements Persona, InventoryHolder {

	private final PersonaSkills skills = new PersonaSkills(this);
	private final PersonaAttributes attributes = new PersonaAttributes(this);

	@Getter String description = null;
	@Getter String prefix = null;
	@Getter Timestamp renamed;
	@Getter double fatigue = 0;
	@Getter ArcheSkin skin;
	
	final AtomicInteger charactersSpoken;
	long lastPlayed;
	long timePlayed;
	int pastPlayTime; //stat_playtime_past
	
	double money = ArcheCore.getEconomyControls().getBeginnerAllowance();
	int food = 20;
	float saturation = 0;
	double health = 20;
	private ArrayList<PotionEffect> effects = Lists.newArrayList();
	
	PersonaInventory inv;
	PlaySession session;
	final Set<String> namelog = Sets.newHashSet();
	
	public ArchePersona(UUID player, int slot, String name, Race race, int birthdate, String gender) { //For NEW personas
		this(handler.getNextPersonaId(), player, slot, name, race, birthdate, gender,
				new Timestamp(System.currentTimeMillis()), PersonaType.NORMAL, null);
	}
	
	public ArchePersona(int persona_id, UUID player, int slot, String name, Race race, int birthdate,
			String gender, Timestamp creationTimeMS, PersonaType type, String raceString) { //For EXISTING personas loaded from drive
		super(new ArchePersonaKey(persona_id, player, slot), creationTimeMS, false, race, birthdate, gender, type, name, raceString);
		charactersSpoken = new AtomicInteger();
		renamed = new Timestamp(0);
		
		lastPlayed = System.currentTimeMillis(); //PersonaStore sets this to 0, then AccountBlob sets this based on sessions
		timePlayed = 0; //AccountBlob sets this based on sum of sessions
		pastPlayTime = 0; //PersonaStore might set this higher
	}

	@Override
	public PersonaSkills skills() {
		return skills;
	}

	@Override
	public PersonaAttributes attributes() {
		return attributes;
	}

	public SkillAttachment getSkill(Skill skill){
		return skills.getSkill(skill);
	}

	@Override
	public void setDateOfBirth(int birthdate) {
		this.dateOfBirth = birthdate;
		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.DATE_OF_BIRTH, birthdate));
	}

	@Override
	public void setGender(String gender) {
		this.gender = gender;
		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.GENDER, gender));
	}

	@Override
	public double withdraw(double amount, Transaction cause) {
		ArcheCore.getControls().getEconomy().withdrawPersona(this, amount, cause);
		return money;
	}

	@Override
	public double deposit(double amount, Transaction cause){
		ArcheCore.getControls().getEconomy().depositPersona(this, amount, cause);
		return money;
	}

	@Override
	public Skill getMainSkill(){
		return skills.getMainProfession();
	}

	@Override
	public void setMainSkill(Skill profession){
		skills.setMainProfession(profession);
		String name = profession == null? null : profession.getName();
		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.SKILL_SELECTED, name));
	}

	public void setCurrent(boolean current) {
		if (this.current != current) {
			this.current = current;
			consumer.queueRow(new UpdatePersonaRow(this, PersonaField.CURRENT, this.current));
		}
	}

	void initSession() {
		this.lastPlayed = System.currentTimeMillis();
		this.session = new PlaySession(this);
	}

	void endSession() {
		this.session.endSession();
		this.lastPlayed = System.currentTimeMillis();
		this.session = null;
	}

	@Override
	public void setPrefix(String prefix){
		this.prefix = prefix;
		updateDisplayName();
		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.PREFIX, prefix));
	}

	@Override
	public boolean hasPrefix(){
		return prefix != null && !prefix.isEmpty();
	}

	@Override
	public void clearPrefix(){
		prefix = null;
		updateDisplayName();
		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.PREFIX, prefix));
	}

	public void updateDisplayName() {
		Player p = getPlayer();
		if (handler.willModifyDisplayNames() && p != null) {
			if (hasPrefix() && ArcheCore.getPlugin().arePrefixesEnabled()) p.setDisplayName("[" + getPrefix() + "] " + name);
			else p.setDisplayName(name);
		}
	}

	public void addTimePlayed(long timePlayed){
		this.timePlayed += timePlayed;
	}
	
	public void compareLastPlayed(long lastPlayed) {
		this.lastPlayed = Math.max(lastPlayed, this.lastPlayed);
	}
	
	@Override
	public long getLastSeen() {
		return this.lastPlayed;
	}
	
	@Override
	public int getTotalPlaytime() {
		return pastPlayTime + getTimePlayed();
	}
	
	@Override
	public int getTimePlayed() {
		return (int) timePlayed;
	}

	@Override
	public int getCharactersSpoken(){
		return charactersSpoken.get();
	}

	/**
	 * Atomically adds an amount of Characters spoken by this Persona
	 * @param charsSpoken Amount of spoken characters to add.
	 */
	public void addCharactersSpoken(int charsSpoken){
		int val = charactersSpoken.addAndGet(charsSpoken);

		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.STAT_CHARS, val));
	}

	@Override
	public Account getAccount() {
		return ArcheCore.getControls().getAccountHandler().getAccount(this.getPlayerUUID());
	}
	
	@Override
	public Player getPlayer(){
		return Bukkit.getPlayer(getPlayerUUID());
	}

	@Override
	public OfflinePlayer getOfflinePlayer() {
		Player play = getPlayer();
		if(play != null) return play;
		return super.getOfflinePlayer();
	}

	@Override
	public UUID getPlayerUUID(){
		return personaKey.getPlayerUUID();
	}

	

	@Override
	public String getChatName(){
		if(prefix == null || prefix.isEmpty()){
			return getName();
		} else {
			return "[" + getPrefix() + "] " + getName();
		}
	}

	@Override
	public Set<String> getPastNames(){
		return Collections.unmodifiableSet(namelog);
	}

	@Override
	public void setName(String name) {
		PersonaRenameEvent event = new PersonaRenameEvent(this, name);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;

		this.name = name;
		renamed = new Timestamp(System.currentTimeMillis());

		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.NAME, name));
		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.STAT_RENAMED, renamed));
		if(namelog.add(name)) consumer.queueRow(new NamelogRow(this.getPersonaId(), name));

		if (current) updateDisplayName();
	}

	@Override
	public void setPersonaType(PersonaType type) {
		this.personaType = type;

		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.TYPE, type));
	}
	
	@Override
	public void setRace(Race r) {
		this.race = r;
		if (ArcheCore.getControls().areRacialBonusesEnabled()) {
			Player p = getPlayer();
			if (p != null && this.isCurrent()) {
				RaceBonusHandler.reset(p);
				RaceBonusHandler.apply(this);
				p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			}
		}
		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.RACE_REAL, race.name()));
		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.RACE, ""));
		this.raceString = null;
	}

	@Override
	public void setApparentRace(String race){
		raceString = race;
		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.RACE, race));
	}
	
	@Override
	public void clearDescription(){
		description = null;
		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.DESCRIPTION, null));
	}

	@Override
	public void addDescription(String addendum){
		if(description == null) description = addendum;
		else description = description + " " + addendum;

		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.DESCRIPTION, description));
	}

	@Override
	public void setDescription(String description) {
		this.description = description;

		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.DESCRIPTION, description));
	}

	void saveMinecraftSpecifics(final Player p){
		//Store and switch Persona-related specifics: Location and Inventory.
		food = p.getFoodLevel();
		health = p.getHealth();
		saturation = p.getSaturation();
		inv = PersonaInventory.store(this);
		inv.saveDiff(); //Queues row. Must be done before the replace statement below
		
		location = new WeakBlock(p.getLocation());
		String pots = savePotionEffects(p);
		CoreLog.debug("Player world is " + p.getWorld().getName() + " which has a UID of " + p.getWorld().getUID().toString());
		
		consumer.replace(PersonaTable.VITALS.getTable())
			.set("persona_id_fk", this.getPersonaId())
			.set("world", p.getWorld().getUID())
			.set("x", location.getX())
			.set("y", location.getY())
			.set("z", location.getZ())
			.set("inv", inv == null? null : inv.getInvAsString())
			.set("ender_inv", inv == null? null: inv.getEnderInvAsString())
			.set("potions", pots)
			.set("health", health)
			.set("hunger", food)
			.set("saturation", saturation)
			.queue();
	}

	private String savePotionEffects(Player pl) {
		effects = Lists.newArrayList(pl.getActivePotionEffects());
		YamlConfiguration config = new YamlConfiguration();
		List<Map<String, Object>> contentslist = Lists.newArrayList();
		for (PotionEffect pe : effects) {
			if (pe == null) {
				contentslist.add(null);
			} else {
				contentslist.add(pe.serialize());
			}
		}
		config.set("potions", contentslist);
		return config.saveToString();
	}

	void loadPotionsFromString(String contents) {
		if (contents == null || contents.isEmpty()) {
			effects = Lists.newArrayList();
			return;
		}
		YamlConfiguration potionconfig = new YamlConfiguration();

		try {
			potionconfig.loadFromString(contents);
			if (potionconfig.getKeys(false).contains("potions")) {
				@SuppressWarnings("unchecked")
				List<PotionEffect> result = potionconfig.getList("potions").stream()
				.map(ent -> (Map<String, Object>) ent)
				.map(ent -> ent == null ? null : new PotionEffect(ent))
				.collect(Collectors.toList());
				effects = Lists.newArrayList(result);
			}
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}

	}

	void restoreMinecraftSpecifics(){
		Player p = getPlayer();
		
		// Dismount them if they're mounted. Also dismount anyone on them.
		if (p.isInsideVehicle()) {
			p.leaveVehicle();
		}
		p.eject();

		//Teleport the Player to the new Persona's stored location
		if(location != null) {
			Bukkit.getScheduler().runTaskLater(ArcheCore.getPlugin(),
					() -> p.teleport(location.toLocation().add(0.5, 0.5, 0.5))
					, 3);
		}

		//Do we protect incase of bad teleport?
		if (ArcheCore.getPlugin().teleportProtectively()) {
			NewbieProtectListener.bonusProtects.add(p.getUniqueId());
			Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), () -> NewbieProtectListener.bonusProtects.remove(p.getUniqueId()));
		}

		//Give them an inventory.
		PlayerInventory pinv = p.getInventory();
		Inventory einv = p.getEnderChest();
		if(inv != null){ //Grab inv from Persona file
			pinv.setContents(inv.getContents());
			einv.setContents(inv.getEnderContents());
			inv = null; //Protect against dupes just in case
		} else { //Clears the inv
			pinv.clear();
			einv.clear();
		}

		//Heal them so their Persona is fresh
		double maxHp = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

		if (maxHp < health) p.setHealth(maxHp);
		else p.setHealth(health);
		p.setFoodLevel(food);
		p.setSaturation(saturation);
		//Add their potion effects back :)
		p.getActivePotionEffects().stream().forEach(e -> p.removePotionEffect(e.getType()));
		effects.forEach(e -> e.apply(p));
	}

	@Override
	public void remove() {
		Player p = Bukkit.getPlayer(getPlayerUUID());

		if(p != null && isCurrent()) {
			boolean hasOtherPersonas = false;
			ArchePersona[] allPersonas = handler.getAllPersonas(p);
			for(int i = 0; i < allPersonas.length; i++) {
				if(allPersonas[i] != null && allPersonas[i] != this){
					handler.switchPersona(p, i, true);
					hasOtherPersonas = true;
					break;
				}
			}

			if(!hasOtherPersonas) {
				CoreLog.warning("Player " + getPlayerName() + " removed his final usable Persona!");
				RaceBonusHandler.reset(p); //Clear Racial bonuses, for now...
				if(p.hasPermission("archecore.mayuse") && !p.hasPermission("archecore.exempt")) new CreationDialog().makeFirstPersona(p);
			}
		}

		super.remove();
	}

	@Override
	public Inventory getInventory() {
		if (current && getPlayer() != null) {
			return getPlayer().getInventory();
		} else {
			return inv.getInventory();
		}
	}

	@Override
	public Inventory getEnderChest() {
		if (current && getPlayer() != null) {
			return getPlayer().getEnderChest();
		} else {
			return inv.getEnderInventory();
		}
	}
	
	@Override
	public boolean isNewbie() {
		if(!doNewbie()) return false;
		return attributes().hasModifier(AttributeRegistry.HUNGER, newbieAttribute());
	}
	
	@Override
	public void setNewbie(boolean newbie) {
		if(!doNewbie()) return;
		
		if(newbie) attributes().addModifier(AttributeRegistry.HUNGER, newbieAttribute());
		else attributes().removeModifier(AttributeRegistry.HUNGER, newbieAttribute());
	}
	
	private boolean doNewbie() {
		return ArcheCore.getPlugin().getNewbieNotificationDelay() > 0;
	}
	
	private AttributeModifier newbieAttribute() {
		long timeInTicks = 20L * 60L * ArcheCore.getPlugin().getNewbieNotificationDelay();
		return new ModifierBuilder()
				.uuid(UUID.fromString("fff5713f-8da2-49e3-8ffb-57bad2a5a166"))
				.name("Newbie Protection")
				.amount(-0.80)
				.operation(Operation.ADD_SCALAR)
				.withDecayStrategy(Decay.ACTIVE, timeInTicks)
				.create();
	}

	@Override
	public void setFatigue(double fatigue) {
		PersonaFatigueEvent event = new PersonaFatigueEvent(this, fatigue);
		Bukkit.getPluginManager().callEvent(event);
		if(!event.isCancelled()) {
			this.fatigue = event.getNewFatigue();
			consumer.queueRow(new UpdatePersonaRow(this, PersonaField.FATIGUE, fatigue));
		}
	}
	
	public void setFatigueRaw(double fatigue) {
		this.fatigue = fatigue;
	}

	@Override
	public void setSkin(ArcheSkin skin) {
		this.skin = skin;
		skin.addPersona(this);

		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.SKIN, skin.getIndex()));
	}

	@Override
	public void removeSkin() {
		skin.removePersona(this);
		this.skin = null;

		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.SKIN, -1));
	}

	@Override
	public boolean hasSkin() {
		return skin != null;
	}

	@Override
	public boolean isLoaded() {
		return true;
	}

	@Override
	public ArchePersona getPersona() {
		return this;
	}
}