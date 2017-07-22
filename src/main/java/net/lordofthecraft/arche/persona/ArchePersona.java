package net.lordofthecraft.arche.persona;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.WeakBlock;
import net.lordofthecraft.arche.enums.ProfessionSlot;
import net.lordofthecraft.arche.enums.SkillTier;
import net.lordofthecraft.arche.event.PersonaRemoveEvent;
import net.lordofthecraft.arche.event.PersonaRenameEvent;
import net.lordofthecraft.arche.event.PersonaSwitchEvent;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.interfaces.Transaction;
import net.lordofthecraft.arche.listener.NewbieProtectListener;
import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.magic.MagicData;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.SaveExecutorManager;
import net.lordofthecraft.arche.save.tasks.DataTask;
import net.lordofthecraft.arche.save.tasks.magic.MagicCreateCallable;
import net.lordofthecraft.arche.save.tasks.persona.PersonaSwitchTask;
import net.lordofthecraft.arche.save.tasks.persona.SelectSkillTask;
import net.lordofthecraft.arche.save.tasks.persona.TagAttachmentCallable;
import net.lordofthecraft.arche.save.tasks.persona.UpdateTask;
import net.lordofthecraft.arche.save.tasks.skills.UpdateSkillSlotTask;
import net.lordofthecraft.arche.skill.ArcheSkill;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import net.lordofthecraft.arche.skill.SkillData;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.ref.WeakReference;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class ArchePersona implements Persona, InventoryHolder {
	private static final String TABLE = "persona";

	private static final ArchePersonaHandler handler = ArchePersonaHandler.getInstance();
	//private static final SaveHandler buffer = SaveHandler.getInstance();
	private static final SaveExecutorManager exe = SaveExecutorManager.getInstance();
	private static CallableStatement deleteCall = null;

	private final UUID persona_id;



	final Map<String,Object> sqlCriteria;
	final AtomicInteger timePlayed;
	final AtomicInteger charactersSpoken;
	private final ArchePersonaKey key;
	private final int gender;
	private final List<SkillAttachment> profs = Lists.newArrayList();
	private final List<MagicAttachment> magics = Lists.newArrayList();
	//public Skill[] professions = new Skill[3];
	int age;
	String description = null;
	volatile String prefix = null;
	boolean current = true;
	boolean autoAge;
	String raceHeader = null;
	long lastRenamed;
	long creationTimeMS;
	int pastPlayTime; //stat_playtime_past
	//Player name no longer final or private
	//We must be able to change it in case of relogs/preloads
	//Where the player has changed their username with Mojang
	String player;
	WeakBlock location = null;
	PersonaInventory inv = null;
	PersonaSkin skin = null;
	double money = 0;
	boolean gainsXP = true;
	Skill profession = null; /*professionPrimary = null, professionSecondary = null, professionAdditional = null;*/
	private Race race;
	private volatile String name;
	private WeakReference<Player> playerObject;
	private int hash = 0;
	private int food = 0;
	private double health = 0;
	private TagAttachment attachment;
	private Map<String,String> tags;
	private String type;
	
	private ArchePersona(UUID persona_id, int id, String name, Race race, int gender, int age,long creationTimeMS) {
		this.persona_id = persona_id;
		key = new ArchePersonaKey(UUID.randomUUID(),id);
		player = name;
		this.race = race;
		this.name = name;
		this.gender = gender;
		this.age = age;
		this.creationTimeMS = creationTimeMS;

		timePlayed = new AtomicInteger();
		charactersSpoken = new AtomicInteger();
		lastRenamed = 0;
		pastPlayTime = 0;

		tags = Maps.newConcurrentMap();

		sqlCriteria = Maps.newHashMap();
		sqlCriteria.put("persona_id", persona_id);
		//sqlCriteria.put("player", getPlayerUUID().toString());
		//sqlCriteria.put("id", id);
	}

	ArchePersona(UUID persona_id, OfflinePlayer p, int id, String name, Race race, int gender, int age,long creationTimeMS){
		this.persona_id = persona_id;
		this.key = new ArchePersonaKey(p.getUniqueId(), id);

		player = p.getName();
		this.race = race;
		this.name = name;
		this.gender = gender;
		this.age = age;
		this.creationTimeMS = creationTimeMS;

		timePlayed = new AtomicInteger();
		charactersSpoken = new AtomicInteger();
		lastRenamed = 0;
		pastPlayTime = 0;

		tags = Maps.newConcurrentMap();

		sqlCriteria = Maps.newHashMap();
		sqlCriteria.put("player", persona_id.toString());
	}

	public static ArchePersona buildTestPersona() {
		return new ArchePersona(UUID.randomUUID(), 0, "test", null, 0, 0, 0);
	}

	public void addSkill(ArcheSkill skill, FutureTask<SkillData> future){
		if(profs.size() != skill.getId()){
			Logger log = ArcheCore.getPlugin().getLogger();
			log.severe("Incorrect skill ordering in Persona LinkedList!");
			log.severe("Expect length " + skill.getId() + " but got " + profs.size() + " for Persona: " + player + "_" + getId());
			log.severe("Will see errors and incorrect xp assignments of skills!");
		}

		SkillAttachment attach = new SkillAttachment(skill, this, future);
		profs.add(attach);
	}

	public SkillAttachment getSkill(int skillId){
		return profs.get(skillId);
	}

	@Override
	public double resetSkills(float mod){
		if (mod > 1) mod = 1f;
		double xp = 0;
		for(Skill s : ArcheSkillFactory.getSkills().values()){
			if(s.isVisible(this)){
				final double val = s.reset(this);
				xp += val;
			}
		}
		xp = xp*mod;
		ArcheSkillFactory.getSkill("internal_drainxp").addRawXp(this, xp, false);
		return xp;
	}

	@Override
	public void racialReassign(Race r){
		setRace(r);
		this.reskillRacialReassignment();
	}

	public PersonaSkin getSkin() {
		return skin;
	}

	@Override
	public boolean hasTagKey(String s) {
		return tags.containsKey(s);
	}

	@Override
	public Optional<String> getTagValue(String tag) {
		String s = attachment.getValue(tag);
		if (s == null) {
			return Optional.empty();
		} else {
			return Optional.of(s);
		}
	}

	@Override
	public Map<String, String> getTags() {
		return attachment.getTags();
	}

	@Override
	public void setTag(String name, String value) {
		attachment.setValue(name, value);
	}

	@Override
	public void removeTag(String name) {
		attachment.delValue(name);
	}

	@Override
	public String getPersonaType() {
		return type;
	}

	@Override
	public void setPersonaType(String type) {
		this.type = type;

		exe.submit(new UpdateTask(this, PersonaField.TYPE, type));
	}

	public void setSkin(PersonaSkin skin) {
		this.skin = skin;
		exe.submit(new UpdateTask(this, PersonaField.SKIN, skin.getData()));
		//if (this.getPlayer() != null) PersonaSkinListener.updatePlayerSkin(this.getPlayer());
	}

	public double reskillRacialReassignment() {
		List<Skill> skills = getOrderedProfessions();
		boolean canHaveBonus = this.getTimePlayed() / 60 >= 250;
		double lostXP = 0;
		boolean main = false;
		boolean second = false;
		boolean bonus = false;
		//this.professions = new Skill[3];
		for (Skill sk : skills) {
			System.out.println(sk.getName() + " " + sk.getXp(this));
			if (sk.getXp(this) <= 0) {
				//System.out.println("inept");
			} else if (sk.isProfessionFor(race)) {
				System.out.println("racial");
			} else if (sk.getXp(this) <= sk.getCapTier(this).getXp()) {
			} else if (!bonus && canHaveBonus) {
				this.setProfession(ProfessionSlot.ADDITIONAL, sk);
				//System.out.println("setting bonus");
				bonus = true;
			} else if (!main) {
				this.setProfession(ProfessionSlot.PRIMARY, sk);
				main = true;
			} else if (!second) {
				this.setProfession(ProfessionSlot.SECONDARY, sk);
				//System.out.println("setting second");
				second = true;
			} else {
				lostXP += sk.getXp(this);
				//System.out.println("adding to pool");
			}
		}
		this.handleProfessionSelection();
		return lostXP;
	}
	/*
	@SuppressWarnings("deprecation")
	public void broadcastSkinChange() {
		// TODO
		Player p = this.getPlayer();
		final ProtocolManager man = ProtocolLibrary.getProtocolManager();
		final List<Player> tracked = Lists.newArrayList();
		for (Player x : p.getWorld().getPlayers()) {
			if (x == p) continue;
			if (p.getLocation().distance(x.getLocation()) < 96) {
				tracked.add(x);
			}
		}
		PacketContainer remove = man.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
		remove.getIntegerArrays().write(0, new int[]{p.getEntityId()});

		for (Player x : tracked) {
			try {
				man.sendServerPacket(x, remove);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}

		PacketContainer update = man.createPacket(PacketType.Play.Server.PLAYER_INFO);
		List<PlayerInfoData> pfile = Arrays.asList(new PlayerInfoData(
				WrappedGameProfile.fromPlayer(p),
				0,
				NativeGameMode.SURVIVAL,
				null));
		update.getPlayerInfoDataLists().write(0, pfile); 
		update.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
		man.broadcastServerPacket(update);
		update.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
		man.broadcastServerPacket(update);

		WrapperPlayServerNamedEntitySpawn add = new WrapperPlayServerNamedEntitySpawn();
		add.setEntityId(p.getEntityId());
		add.setPlayerUuid(p.getUniqueId());
		add.setPosition(p.getLocation().toVector());
		add.setCurrentItem(p.getItemInHand().getTypeId());

		Location head = p.getEyeLocation();
		add.setPitch(head.getPitch());
		add.setYaw(head.getYaw());

		WrappedDataWatcher watcher = new WrappedDataWatcher();
		watcher.setObject(10, ((CraftPlayer)p).getHandle().getDataWatcher().getByte(10));
		watcher.setObject(6, (float)p.getHealth());
		add.setMetadata(watcher);

		for (Player x : tracked) {
			add.sendPacket(x);
		}
	}*/

	@Override
	public double withdraw(double amount, Transaction cause) {
		ArcheCore.getControls().getEconomy().withdrawPersona(this, amount);
		return money;
	}

	@Override
    @Deprecated
    public double withdraw(double amount) {
        ArcheCore.getControls().getEconomy().withdrawPersona(this, amount);
        return money;
    }

    @Override
	public double deposit(double amount, Transaction cause){
		ArcheCore.getControls().getEconomy().depositPersona(this, amount);
		return money;
	}

    @Override
    @Deprecated
    public double deposit(double amount) {
        ArcheCore.getControls().getEconomy().depositPersona(this, amount);
        return money;
    }

    public Skill[] getArrayOfProfessions() {
		Skill[] skills = new Skill[3];
		if (getProfession(ProfessionSlot.PRIMARY).isPresent()) {
			skills[0] = getProfession(ProfessionSlot.PRIMARY).get();
		} else {
			skills[0] = null;
		}
		if (getProfession(ProfessionSlot.SECONDARY).isPresent()) {
			skills[1] = getProfession(ProfessionSlot.SECONDARY).get();
		} else {
			skills[1] = null;
		}
		if (getProfession(ProfessionSlot.ADDITIONAL).isPresent()) {
			skills[2] = getProfession(ProfessionSlot.ADDITIONAL).get();
		} else {
			skills[2] = null;
		}
		return skills;
	}

	public List<Skill> getOrderedProfessions() {
		List<Skill> skills = Lists.newArrayList();
		skills.addAll(profs.stream().filter(sk -> sk.skill.isVisible(this)).map(sk -> sk.skill).collect(Collectors.toList()));
		skills.sort(new SkillComparator(this));
		return skills;
	}

	@Override
	public Skill getMainSkill(){
		return profession;
	}

	@Override
	public void setMainSkill(Skill profession){
		this.profession = profession;
		String name = profession == null? null : profession.getName();

		exe.submit(new UpdateTask(this, PersonaField.SKILL_SELECTED, name));
	}

	@Override
	public Optional<Skill> getProfession(ProfessionSlot slot){
		Optional<SkillAttachment> att = profs.stream().filter(s -> s.getSlot() == slot).findFirst();
		return att.map(skillAttachment -> skillAttachment.skill);
		/*return profs.stream().filter(s -> s.getSlot() == slot).findFirst();
		switch(slot){
		case PRIMARY: return professions[0];
		case SECONDARY: return professions[1];
		case ADDITIONAL: return professions[2];
		default: throw new IllegalArgumentException();
		}*/
	}

	@Override
	public boolean isSkillInSlot(Skill s, ProfessionSlot slot) {
		Optional<SkillAttachment> oatt = profs.stream().filter(a -> a.getSkill().equals(s)).findFirst();
		return oatt.filter(skillAttachment -> skillAttachment.getSlot() == slot).isPresent();
	}

	@Override
	public ChatColor colorForSkill(Skill s) {
		Optional<SkillAttachment> oatt = profs.stream().filter(at -> at.getSkill().equals(s)).findFirst();
		if (oatt.isPresent()) {
			SkillAttachment att = oatt.get();
			if (att.getSkill().isProfessionFor(race)) {
				return ChatColor.BLUE;
			}
			switch (att.getSlot()) {
				case PRIMARY:
					return ChatColor.DARK_GREEN;
				case SECONDARY:
					return ChatColor.GREEN;
				case ADDITIONAL:
					return ChatColor.AQUA;
				default:
					return ChatColor.YELLOW;
			}
		} else {
			return ChatColor.YELLOW;
		}
	}

	@Override
	public void setProfession(ProfessionSlot slot, Skill profession){
		//if(professions[slot.getSlot()] == profession) return;
		//professions[slot.getSlot()] = profession;
		Optional<SkillAttachment> att = profs.stream().filter(a -> a.getSkill().equals(profession)).findFirst();
		if (att.isPresent()) {
			SkillAttachment attch = att.get();
			if (attch.getSlot() == slot) {
				return;
			}
			attch.setSlot(slot);
			if (slot != ProfessionSlot.UNSELECTED) {
				profs.stream().filter(a -> a.getSlot() == slot).forEach(a -> {
					ArcheCore.getPlugin().getLogger().info("Deselected the skill "+a.getSkill().getName()+" from the slot "+slot.name()+" for the persona "+persona_id+" for the player "+player);
					a.setSlot(ProfessionSlot.UNSELECTED);
				});

			}
		} else {
			ArcheCore.getPlugin().getLogger().severe("The Persona "+persona_id+" for the player "+player+" had no skill attachment for the skill "+profession.getName()+"!?!?!");
		}

		//String name = profession == null? null : profession.getName();
		exe.submit(new UpdateSkillSlotTask(this, profession, slot));
		//buffer.put(new UpdateTask(this, slot.getPersonaField(), name));
	}

	@Override
	public void deselectProfession(Skill profession) {
		profs.stream().filter(a -> a.getSkill().equals(profession)).forEach(a -> a.setSlot(ProfessionSlot.UNSELECTED));
	}

	@Override
	public void deselectSlot(ProfessionSlot slot) {
		profs.stream().filter(a -> a.getSlot() == slot).forEach(a -> a.setSlot(ProfessionSlot.UNSELECTED));
	}

	public double getXpLost(){
		double xp = 0;
		for(SkillAttachment att : profs){
			if(!att.isInitialized()) att.initialize();
			SkillTier tier = att.skill.getCapTier(this);

			if(tier == SkillTier.RUSTY) {
				xp+= att.getXp();
			} else if(att.getXp() > tier.getXp()) {
				xp+= att.getXp() - tier.getXp();
			}
		}
		return xp;
	}

	public void handleProfessionSelection(){
		for(SkillAttachment att : profs){
			if(!att.isInitialized()) att.initialize();

			if (att.skill.getName().equalsIgnoreCase("internal_drainxp")) continue;

			SkillTier tier = att.skill.getCapTier(this);

			if (att.getXp() > tier.getXp())
				att.setXp(tier.getXp());
			else if(att.getXp() < 0 && tier != SkillTier.RUSTY)
				att.setXp(0);
		}
	}

	@Override
	public int getId(){
		return key.getPersonaId();
	}

	@Override
	public boolean isCurrent(){
		return current;
	}

	void setCurrent(boolean current) {
		if (this.current != current) {

			this.current = current;

			exe.submit(new UpdateTask(this, PersonaField.CURRENT, current));

			if (current) { // Persona becoming Player's current Persona.
				Player p = Bukkit.getPlayer(getPlayerUUID());
				if (p != null) {
					updateDisplayName(p);

					//Apply Racial bonuses
					if (ArcheCore.getControls().areRacialBonusesEnabled())
						RaceBonusHandler.apply(p, race);


				} else {
					ArcheCore.getPlugin().getLogger().info("Player " + player + " was not found (null) as her Persona was switched.");
				}

				/*
				//Start loading the skills for the current Persona ONLY
				if(!profs.isEmpty()){
					profs.clear();
					ArcheCore.getPlugin().getLogger().warning("Skills for persona " + player + "_" + getId() + " expected empty, but wasn't. Resource leak?");
				}


				//Current Persona gets the skills loaded into memory
				loadSkills();
				 */

				//Should now have been done already at player login time for all Personas
			}
		}
	}

	void loadSkills(){
		for(ArcheSkill s : ArcheSkillFactory.getSkills().values()){

			//Start loading this Persona's Skill data for this one particular skill
			SelectSkillTask task = new SelectSkillTask(this, s);
			FutureTask<SkillData> fut = task.getFuture();
			exe.submit(task);

			addSkill(s, fut);
		}
	}

	void loadTags() {
		FutureTask<TagAttachment> task = new FutureTask<>(new TagAttachmentCallable(persona_id, ArcheCore.getSQLControls()));
		try {
			attachment = task.get(200, TimeUnit.MILLISECONDS);

		} catch (TimeoutException e) {
			ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "We timed out while trying to fetch the persona "+persona_id.toString()+"'s tags!", e);
			attachment = new TagAttachment(Maps.newConcurrentMap(), persona_id, false);
		} catch (Exception e) {
			ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "We threw an exception while trying to fetch the persona "+persona_id.toString()+"'s tags!", e);
			attachment = new TagAttachment(Maps.newConcurrentMap(), persona_id, false);
		}
	}

	void loadMagics() {
		String sql = "SELECT magic_id,magic_fk,tier,last_advanced,teacher,learned,visible FROM persona_magic WHERE persona_fk=?";
		try {
			PreparedStatement stat = ArcheCore.getSQLControls().getConnection().prepareStatement(sql);
			stat.setString(1, persona_id.toString());
			ResultSet rs = stat.executeQuery();
			while (rs.next()) {
				MagicData data = null;
				String magic = rs.getString("magic_fk");
				Optional<ArcheMagic> armagic = ArcheMagic.getMagicByName(magic);
				if (armagic.isPresent()) {
					int it = rs.getInt("magic_id");
					int tier = rs.getInt("tier");
					Timestamp last_advanced = rs.getTimestamp("last_advanced");
					Timestamp learned = rs.getTimestamp("learned");
					String teacher = rs.getString("teacher");
					boolean visible = rs.getBoolean("visible");
					data = new MagicData(armagic.get(), it, tier, visible, teacher == null, (teacher == null ? null : UUID.fromString(teacher)), learned.toInstant().toEpochMilli(), last_advanced.toInstant().toEpochMilli());
					magics.add(new MagicAttachment(armagic.get(), persona_id, data));
				}
			}
			rs.close();
			stat.close();
		} catch (SQLException e) {
			ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "An error occurred while loading " + player + "'s persona magics!!!", e);
		}
	}

	void createEmptyTags() {
		attachment = new TagAttachment(Maps.newConcurrentMap(), persona_id, true);
	}

	void removeMagicAttachment(ArcheMagic magic) {
		magics.removeIf(mag -> mag.getMagic().equals(magic));
	}

	public Optional<MagicAttachment> getMagicAttachment(ArcheMagic m) {
		return magics.stream().filter(at -> at.getMagic().equals(m)).findFirst();
	}

	@Override
	public boolean hasMagic(ArcheMagic m) {
		return magics.stream().anyMatch(at -> at.getMagic().equals(m));
	}

	@Override
	public boolean hasAchievedMagicTier(ArcheMagic m, int tier) {
		Optional<MagicAttachment> omat = magics.stream().filter(at -> at.getMagic().equals(m)).findFirst();
		return omat.filter(magicAttachment -> magicAttachment.getTier() >= tier).isPresent();
	}

	@Override
	public Optional<Future<MagicAttachment>> createAttachment(ArcheMagic m, int tier, Persona teacher, boolean visible) {
		if (magics.stream().anyMatch(at -> at.getMagic().equals(m))) {
			return Optional.empty();
		}
		MagicCreateCallable call = new MagicCreateCallable(persona_id, m, tier, (teacher == null ? null : teacher.getPersonaId()), visible, ArcheCore.getSQLControls());
		Future<MagicAttachment> future = exe.call(call);
		try {
			MagicAttachment attach = future.get(200, TimeUnit.MILLISECONDS);

			magics.add(attach);
			return Optional.of(future);
		} catch (TimeoutException e) {
			ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "Timed out while adding a magic to the persona " + persona_id + " (" + player + "). Magic: " + m.getName(), e);
		} catch (Exception e) {
			ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "An error occurred while adding a magic to the persona " + persona_id + " (" + player + "). Magic: " + m.getName(), e);
		}
		return Optional.empty();
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public void setPrefix(String prefix){
		this.prefix = prefix;
		updateDisplayName(Bukkit.getPlayer(this.getPlayerUUID()));
		exe.submit(new UpdateTask(this, PersonaField.PREFIX, prefix));
	}

	@Override
	public boolean hasPrefix(){
		return prefix != null && !prefix.isEmpty();
	}

	@Override
	public void clearPrefix(){
		prefix = null;
		updateDisplayName(Bukkit.getPlayer(this.getPlayerUUID()));
		exe.submit(new UpdateTask(this, PersonaField.PREFIX, prefix));
	}

	void updateDisplayName(Player p){
		if(handler.willModifyDisplayNames() && p != null){
			if(hasPrefix() && ArcheCore.getPlugin().arePrefixesEnabled())
				p.setDisplayName("[" + getPrefix() + "] " + name);
			else
				p.setDisplayName(name);
		}
	}

	@Override
	public boolean getXPGain() {
		return gainsXP;
	}

	@Override
	public void setXPGain(boolean gainsXP){
		if(this.gainsXP != gainsXP){
			this.gainsXP = gainsXP;

			exe.submit(new UpdateTask(this, PersonaField.XP_GAIN, gainsXP));
		}
	}

	@Override
	public int getTimePlayed(){
		return timePlayed.get();
	}

	/**
	 * Atomically adds minutes to the total amount of this Persona's playtime.
	 * @param timePlayed The amount of minutes to add
	 */
	public void addTimePlayed(int timePlayed){
		int val = this.timePlayed.addAndGet(timePlayed);

		exe.submit(new UpdateTask(this, PersonaField.STAT_PLAYED, val));
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

		exe.submit(new UpdateTask(this, PersonaField.STAT_CHARS, val));
	}

	//@Override
	public String getPlayerName(){
		return player;
	}

	@Override
	public PersonaKey getPersonaKey(){
		return key;
	}

	@Override
	public Player getPlayer(){
		Player play;
		if(playerObject == null || (play = playerObject.get()) == null || play.isDead()){
			play = Bukkit.getPlayer(getPlayerUUID());

			if(play == null){
				playerObject = null;
				return null;
			}

			playerObject = new WeakReference<>(play);

		}

		return play;
	}

	@Override
	public UUID getPlayerUUID(){
		return key.getPlayerUUID();
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
	public String getName(){
		return name;
	}

	@Override
	public void setName(String name) {
		PersonaRenameEvent event = new PersonaRenameEvent(this, name);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;

		this.name = name;
		lastRenamed = System.currentTimeMillis();

		exe.submit(new UpdateTask(this, PersonaField.NAME, name));
		exe.submit(new UpdateTask(this, PersonaField.STAT_RENAMED, lastRenamed));

		if (current) {
			Player p = Bukkit.getPlayer(getPlayerUUID());
			updateDisplayName(p);
		}
	}

	@Override
	public Race getRace(){
		return race;
	}

	public void setRace(Race r) {
		this.race = r;
		if (ArcheCore.getControls().areRacialBonusesEnabled()) {
			Player p = getPlayer();
			if (p != null && this.isCurrent()) {
				RaceBonusHandler.reset(p);
				RaceBonusHandler.apply(p, race);
				p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			}
		}
		exe.submit(new UpdateTask(this, PersonaField.RACE_REAL, race));
		exe.submit(new UpdateTask(this, PersonaField.RACE, ""));
		this.raceHeader = null;
	}

	@Override
	public String getRaceString(){
		if(raceHeader == null || raceHeader.isEmpty()){
			return race.getName();
		} else return raceHeader;
	}

	@Override
	public void setApparentRace(String race){
		raceHeader = race;

		exe.submit(new UpdateTask(this, PersonaField.RACE, race));
	}

	@Override
	public long getRenamed(){
		return lastRenamed;
	}

	@Override
	public void clearDescription(){
		description = null;
		exe.submit(new UpdateTask(this, PersonaField.DESCRIPTION, null));
	}

	@Override
	public void addDescription(String addendum){
		if(description == null) description = addendum;
		else description = description + " " + addendum;

		exe.submit(new UpdateTask(this, PersonaField.DESCRIPTION, description));
	}

	@Override
	public String getDescription(){
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;

		exe.submit(new UpdateTask(this, PersonaField.DESCRIPTION, description));
	}

	@Override
	public String getGender(){
		switch(gender){
		case 0: return "Female";
		case 1: return "Male";
		default: return null;
		}
	}

	@Override
	public int getAge(){
		return age;
	}

	@Override
	public void setAge(int age){
		this.age = age;

		exe.submit(new UpdateTask(this, PersonaField.AGE, age));
	}

	@Override
	public boolean doesAutoAge(){
		return autoAge;
	}

	@Override
	public void setAutoAge(boolean autoAge){
		this.autoAge = autoAge;
		exe.submit(new UpdateTask(this, PersonaField.AUTOAGE, autoAge));
	}

	void saveMinecraftSpecifics(final Player p){
		//Store and switch Persona-related specifics: Location and Inventory.
		food = p.getFoodLevel();
		health = p.getHealth();
		inv = PersonaInventory.store(p);
		location = new WeakBlock(p.getLocation());

		exe.submit(new PersonaSwitchTask(this));
	}

	void restoreMinecraftSpecifics(final Player p){

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
			// TODO: This doesn't actually do anything lmao
			new BukkitRunnable(){
			    public void run(){
			        NewbieProtectListener.bonusProtects.remove(p.getUniqueId());
			    }};
		}

		//Give them an inventory.
		PlayerInventory pinv = p.getInventory();
		if(inv != null){ //Grab inv from Persona file
			pinv.setContents(inv.getContents());

			// Legacy code. This is only here to make sure personas that are pre 1.11 load
			if (inv.getArmorContents() != null) {
				pinv.setArmorContents(inv.getArmorContents());
			}
			inv = null; //Protect against dupes just in case
		} else { //Clears the inv
			pinv.clear();
			pinv.setArmorContents(new ItemStack[4]);
		}

		//Heal them so their Persona is fresh
		if (health == 0) {
			health = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		}
		if (food == 0) {
			food = 20;
		}
		if (p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() < health)
			p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		else p.setHealth(health);
		p.setFoodLevel(food);
	}

	@Override
	public boolean remove() {
		if (deleteCall == null) {
			try {
				deleteCall = ArcheCore.getControls().getSQLHandler().getConnection().prepareCall("{call delete_persona(?, ?)}");
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		}
		Player p = Bukkit.getPlayer(getPlayerUUID());

		//We enforce Player is online to do this right now
		Validate.notNull(p);

		PersonaRemoveEvent event = new PersonaRemoveEvent(this, false);
		Bukkit.getPluginManager().callEvent(event);
		if(event.isCancelled()) return false;


		exe.submit(new DataTask(DataTask.DELETE, TABLE, null, sqlCriteria));
		//		Both could be commented out once cascading db is setup!
		exe.submit(new DataTask(DataTask.DELETE, "persona_names", null, sqlCriteria));
		handler.deleteSkills(this);

		ArchePersona[] prs = handler.getAllPersonas(this.getPlayerUUID());
		prs[getId()] = null;

		if(isCurrent()){
			boolean success = false;
			for (ArchePersona pr : prs) {
				if (pr != null) {
					PersonaSwitchEvent ev2 = new PersonaSwitchEvent(pr);
					Bukkit.getPluginManager().callEvent(ev2);
					if (ev2.isCancelled()) continue;

					pr.setCurrent(true);
					pr.restoreMinecraftSpecifics(p);
					success = true;
					break;
				}
			}

			if(!success){
				Plugin plugin = ArcheCore.getPlugin();
				plugin.getLogger().warning("Player " + player + " removed his final usable Persona!");
				RaceBonusHandler.reset(p); //Clear Racial bonuses, for now...
				if(p.hasPermission("archecore.mayuse") && !p.hasPermission("archecore.exempt")) new CreationDialog().makeFirstPersona(p);
			}

			p.sendMessage(ChatColor.DARK_PURPLE + "Your persona was removed: " + ChatColor.GRAY + this.getName());
		}

		return true;
	}

	@Override
	public Inventory getInventory() {
		if (current && getPlayer() != null) {
			return getPlayer().getInventory();
		} else {
			if (inv == null || inv.getContents() == null) {
				return null;
			}
			Inventory binv = Bukkit.createInventory(this, 45, "Persona Inventory: " + getPlayerName()+"@"+getId());
			binv.setContents(inv.getContents());
			return binv;
		}
	}

	public PersonaInventory getPInv() {
		return inv;
	}

	public Location getLocation(){
		if(location == null) return null;
		else return location.toLocation();
	}

	@Override
	public int hashCode(){
		if(hash == 0)
			hash = (5 * this.player.hashCode()) + getId();

		return hash;
	}

	@Override
	public boolean equals(Object object){
		if(object == null) return false;
		if(!(object instanceof ArchePersona)) return false;
		ArchePersona p = (ArchePersona) object;
		return this.player.equals(p.player) && this.getId() == p.getId();
	}
//
	@Override
	public boolean isNewbie() {
		return getTimePlayed() < ArcheCore.getControls().getNewbieDelay();
	}

	private class SkillComparator implements Comparator<Skill> {
		Persona p;

		public SkillComparator(Persona p) {
			this.p = p;
		}

		@Override
		public int compare(Skill o1, Skill o2) {
			return Double.compare(o2.getXp(p), o1.getXp(p));
		}
	}

	@Override
	public long getCreationTime(){
		return this.creationTimeMS;
	}

	@Override
	public int getTotalPlaytime(){
		return pastPlayTime + getTimePlayed();
	}

	@Override
	public UUID getPersonaId() { return persona_id; }
}
