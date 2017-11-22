package net.lordofthecraft.arche.persona;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.persona.PersonaFatigueEvent;
import net.lordofthecraft.arche.event.persona.PersonaRemoveEvent;
import net.lordofthecraft.arche.event.persona.PersonaRenameEvent;
import net.lordofthecraft.arche.event.persona.PersonaSwitchEvent;
import net.lordofthecraft.arche.interfaces.Creature;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.Magic;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.interfaces.Transaction;
import net.lordofthecraft.arche.listener.NewbieProtectListener;
import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.magic.MagicData;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.rows.magic.insert.MagicInsertRow;
import net.lordofthecraft.arche.save.rows.persona.DeletePersonaRow;
import net.lordofthecraft.arche.save.rows.persona.UpdatePersonaRow;
import net.lordofthecraft.arche.save.rows.persona.UpdateVitalsRow;
import net.lordofthecraft.arche.skin.ArcheSkin;
import net.lordofthecraft.arche.skin.SkinCache;
import net.lordofthecraft.arche.util.WeakBlock;

public final class ArchePersona extends ArcheOfflinePersona implements Persona, InventoryHolder {
	private static final IConsumer consumer = ArcheCore.getConsumerControls();
	private static final ArchePersonaHandler handler = ArchePersonaHandler.getInstance();

	private final PersonaSkills skills = new PersonaSkills(this);
    private final PersonaMagics magics = new PersonaMagics(this);
    private final PersonaAttributes attributes = new PersonaAttributes(this);
    
	final Map<String,Object> sqlCriteria;
	final AtomicInteger charactersSpoken;
    String description = null;
	volatile String prefix = null;
	boolean current = true;
	String raceHeader = null;
	Timestamp lastRenamed;
    int pastPlayTime; //stat_playtime_past
	String player; //Last known minecraft name of the owning player
	double money = ArcheCore.getEconomyControls().getBeginnerAllowance();
	double fatigue = 0;
	int food = 20;
    float saturation = 0;
    double health = 20;
    PersonaInventory inv;
	private Creature creature;
	private WeakReference<Player> playerObject;
	
    ArcheSkin skin;
    private ArrayList<PotionEffect> effects = Lists.newArrayList();

    ArchePersona(int persona_id, UUID player, int slot, String name, Race race, String gender, Timestamp creationTimeMS, Timestamp lastPlayed) {
        this(persona_id, player, slot, name, race, gender, creationTimeMS, lastPlayed, PersonaType.NORMAL);
    }

    ArchePersona(int persona_id, UUID player, int slot, String name, Race race, String gender, Timestamp creationTimeMS, Timestamp lastPlayed, PersonaType type) {
        super(new ArchePersonaKey(persona_id, player, slot), creationTimeMS, lastPlayed, false, race, gender, type, name);
		charactersSpoken = new AtomicInteger();
		lastRenamed = new Timestamp(0);
		pastPlayTime = 0;

		sqlCriteria = Maps.newConcurrentMap();
		sqlCriteria.put("persona_id", persona_id);
	}

	@Override
    public int getPersonaId() {
        return personaKey.getPersonaID();
    }
	
    @Override
    public int getTotalPlaytime() {
        return pastPlayTime + getTimePlayed();
    }

	public PersonaSkills skills() {
		return skills;
	}

	public PersonaAttributes attributes() {
		return attributes;
	}
	
	public SkillAttachment getSkill(Skill skill){
		return skills.getSkill(skill);
	}
	
    @Override
    public void setGender(String gender) {
        this.gender = gender;
        consumer.queueRow(new UpdatePersonaRow(this, PersonaField.GENDER, gender));
    }

	public void setPlayerName(String name) {
        this.player = name;
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


	@Override
    public int getSlot() {
        return personaKey.getPersonaSlot();
    }

	@Override
	public boolean isCurrent(){
		return current;
	}

    @Override
    public PersonaMagics magics() {
        return magics;
    }

    void setCurrent(boolean current) {
        if (this.current != current) {

			this.current = current;
            consumer.queueRow(new UpdatePersonaRow(this, PersonaField.CURRENT, this.current));

            if(current) {
            	Validate.notNull(getPlayer(), "Persona can't be switched while Player offline");
            	updateDisplayName();
            	RaceBonusHandler.apply(this);
            }
		}
	}

	void removeMagicAttachment(Magic magic) {
        magics.removeMagicAttachment(magic);
    }

	public Optional<MagicAttachment> getMagicAttachment(Magic m) {
        return magics.getMagicAttachment(m);
    }

	@Override
	public boolean hasMagic(Magic m) {
        return magics.hasMagic(m);
    }

	@Override
	public boolean hasAchievedMagicTier(Magic m, int tier) {
        return magics.achievedTier(m, tier);
    }

	@Override
    public Optional<MagicAttachment> createAttachment(Magic m, int tier, Persona teacher, boolean visible) {
        if (magics.hasMagic(m)) {
            return getMagicAttachment(m);
        }
        MagicData data = new MagicData(m, tier, visible, teacher != null, (teacher == null ? null : teacher.getPersonaId()), System.currentTimeMillis(), System.currentTimeMillis());
        consumer.queueRow(new MagicInsertRow(this, (ArcheMagic) m, tier, (teacher == null ? null : teacher.getPersonaId()), visible));
        return Optional.of(new MagicAttachment(m, this, data));
    }

	@Override
	public String getPrefix() {
		return prefix;
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

	void updateDisplayName(){
		Player p = getPlayer();
		if(handler.willModifyDisplayNames() && p != null){
			if(hasPrefix() && ArcheCore.getPlugin().arePrefixesEnabled())
				p.setDisplayName("[" + getPrefix() + "] " + name);
			else
				p.setDisplayName(name);
		}
	}

	/**
	 * Atomically adds minutes to the total amount of this Persona's playtime.
	 * @param timePlayed The amount of minutes to add
	 */
	public void addTimePlayed(int timePlayed){
		int val = this.timePlayed.addAndGet(timePlayed);

        consumer.queueRow(new UpdatePersonaRow(this, PersonaField.STAT_PLAYED, val));
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

	//@Override
	public String getPlayerName(){
		return player;
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
        return personaKey.getPlayerUUID();
    }

    @Override
    public String getRaceString(boolean mod) {
        StringBuilder sb = new StringBuilder();
        if (magics.hasCreature()) {
            sb.append(magics.getCreature().getName()).append(" ");
            if (mod) {
                sb.append(ChatColor.GRAY);
                String end = "";
                if (raceHeader != null && !raceHeader.isEmpty()) {
                    sb.append("[").append(raceHeader).append(" ");
                    end = ChatColor.GRAY + "]";
                }
                sb.append(ChatColor.DARK_GRAY).append("(").append(race.getName()).append(")");
                sb.append(end);
            }
        } else if (raceHeader != null && !raceHeader.isEmpty()) {
            if (mod) {
                sb.append(ChatColor.GRAY).append("(").append(race.getName()).append(") ");
            }
        } else {
            if (race != Race.UNSET) {
                sb.append(race.getName());
            } else if (mod) {
                sb.append(ChatColor.GRAY).append(race.getName());
            }
        }
        return sb.toString();
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
    public void setName(String name) {
        PersonaRenameEvent event = new PersonaRenameEvent(this, name);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        this.name = name;
        lastRenamed = new Timestamp(System.currentTimeMillis());

        consumer.queueRow(new UpdatePersonaRow(this, PersonaField.NAME, name));
        consumer.queueRow(new UpdatePersonaRow(this, PersonaField.STAT_RENAMED, lastRenamed));

        if (current) updateDisplayName();
    }
    
    @Override
    public void setPersonaType(PersonaType type) {
        this.type = type;

        consumer.queueRow(new UpdatePersonaRow(this, PersonaField.TYPE, type));
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
                RaceBonusHandler.apply(this);
                p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			}
		}
        consumer.queueRow(new UpdatePersonaRow(this, PersonaField.RACE_REAL, race.name()));
        consumer.queueRow(new UpdatePersonaRow(this, PersonaField.RACE, ""));
        this.raceHeader = null;
	}

	@Override
	public void setApparentRace(String race){
		raceHeader = race;

        consumer.queueRow(new UpdatePersonaRow(this, PersonaField.RACE, race));
    }

	@Override
	public Timestamp getRenamed() {
		return lastRenamed;
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
	public String getDescription(){
		return description;
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
		location = new WeakBlock(p.getLocation());
        String pots = savePotionEffects(p);
        ArcheCore.getPlugin().getLogger().info("Player world is " + p.getWorld().getName() + " which has a UID of " + p.getWorld().getUID().toString());
        consumer.queueRow(new UpdateVitalsRow(this, p.getWorld().getUID(), location.getX(), location.getY(), location.getZ(), health, saturation, food, inv, pots));
        attributes.handleSwitch(false);
    }

    String savePotionEffects(Player pl) {
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
        attributes.handleSwitch(false);

		//Heal them so their Persona is fresh
		double maxHp = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();

		if (maxHp < health) p.setHealth(maxHp);
		else p.setHealth(health);
		p.setFoodLevel(food);
        p.setSaturation(saturation);
        //Add their potion effects back :)
        p.getActivePotionEffects().parallelStream().forEach(e -> p.removePotionEffect(e.getType()));
        effects.forEach(e -> e.apply(p));
    }

	@Override
	public boolean remove() {
		Player p = Bukkit.getPlayer(getPlayerUUID());

		//We enforce Player is online to do this right now
		Validate.notNull(p);

		PersonaRemoveEvent event = new PersonaRemoveEvent(this, false);
		Bukkit.getPluginManager().callEvent(event);

        consumer.queueRow(new DeletePersonaRow(this));

		ArchePersona[] prs = handler.getAllPersonas(this.getPlayerUUID());
        prs[getSlot()] = null;

		SkinCache cache = ArcheCore.getControls().getSkinCache();
		boolean newPersonaHasSkin = false;
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
                    newPersonaHasSkin = pr.hasSkin();
                    break;
				}
			}

			boolean cleared = cache.clearSkin(this);
			if (!success) {
				Plugin plugin = ArcheCore.getPlugin();
				plugin.getLogger().warning("Player " + player + " removed his final usable Persona!");
				RaceBonusHandler.reset(p); //Clear Racial bonuses, for now...
				if(p.hasPermission("archecore.mayuse") && !p.hasPermission("archecore.exempt")) new CreationDialog().makeFirstPersona(p);
			} else {
				if(!cleared && newPersonaHasSkin) cache.refreshPlayer(p);
			}

			p.sendMessage(ChatColor.DARK_PURPLE + "Your persona was removed: " + ChatColor.GRAY + this.getName());
		}
        deleted = true;
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
            Inventory binv = Bukkit.createInventory(this, InventoryType.PLAYER.getDefaultSize(), "Persona Inventory: " + player + "@" + getSlot());
            binv.setContents(inv.getContents());
			return binv;
		}
	}

    @Override
    public Inventory getEnderChest() {
        if (current && getPlayer() != null) {
            return getPlayer().getEnderChest();
        } else {
            if (inv == null || inv.getEnderContents() == null) {
                return null;
            }
            Inventory einv = Bukkit.createInventory(this, InventoryType.ENDER_CHEST.getDefaultSize(), "Persona Enderchest: " + player + "@" + getSlot());
            einv.setContents(inv.getEnderContents());
            return einv;
        }
    }

	public PersonaInventory getPInv() {
		return inv;
	}


	
	@Override
	public boolean isNewbie() {
		return getTimePlayed() < ArcheCore.getControls().getNewbieDelay();
	}

	@Override
	public Timestamp getCreationTime() {
        return creation;
    }

	@Override
	public double getFatigue() {
		return fatigue;
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
    public OfflinePersona unloadPersona() {
        return new ArcheOfflinePersona(personaKey, creation, lastPlayed, current, race, gender, type, name);
    }

    @Override
    public ArcheSkin getSkin() {
        return skin;
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

    @Override
    public Persona loadPersona() {
        return this;
    }
}