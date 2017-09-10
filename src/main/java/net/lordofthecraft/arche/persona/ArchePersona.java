package net.lordofthecraft.arche.persona;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.WeakBlock;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.PersonaFatigueEvent;
import net.lordofthecraft.arche.event.PersonaRemoveEvent;
import net.lordofthecraft.arche.event.PersonaRenameEvent;
import net.lordofthecraft.arche.event.PersonaSwitchEvent;
import net.lordofthecraft.arche.interfaces.Creature;
import net.lordofthecraft.arche.interfaces.Magic;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.interfaces.Transaction;
import net.lordofthecraft.arche.listener.NewbieProtectListener;
import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.magic.MagicData;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.magic.MagicCreateCallable;
import net.lordofthecraft.arche.save.tasks.persona.PersonaDeleteTask;
import net.lordofthecraft.arche.save.tasks.persona.TagAttachmentCallable;
import net.lordofthecraft.arche.save.tasks.persona.UpdateFatigueTask;
import net.lordofthecraft.arche.save.tasks.persona.UpdateTask;
import net.lordofthecraft.arche.save.tasks.persona.UpdateVitalsTask;
import net.lordofthecraft.arche.save.tasks.skills.SelectSkillTask;
import net.lordofthecraft.arche.skill.ArcheSkill;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import net.lordofthecraft.arche.skill.SkillData;
import net.lordofthecraft.arche.skin.ArcheSkin;
import net.lordofthecraft.arche.skin.SkinCache;

public final class ArchePersona implements Persona, InventoryHolder {
	private static final String TABLE = "persona";

	private static final ArchePersonaHandler handler = ArchePersonaHandler.getInstance();
	private static final SaveHandler buffer = SaveHandler.getInstance();

	//The immutable auto-increment ID of this persona.
    private final int persona_id;

	final PersonaSkills skills = new PersonaSkills(this);
    final PersonaMagics magics = new PersonaMagics(this);
    final PersonaAttributes attributes = new PersonaAttributes(this);

	final Map<String,Object> sqlCriteria;
	final AtomicInteger timePlayed;
	final AtomicInteger charactersSpoken;
	private final ArchePersonaKey key;
    private String gender;
    String description = null;
	volatile String prefix = null;
	boolean current = true;
	String raceHeader = null;
	Timestamp lastRenamed;
    private Timestamp creationTimeMS;
    int pastPlayTime; //stat_playtime_past
	String player; //Last known minecraft name of the owning player
	WeakBlock location = null;
	PersonaInventory inv = null;
	double money = 0;
	double fatigue = 0;
	int food = 0;
    float saturation = 0;
    double health = 0;
	Timestamp lastPlayed;
	private Race race;
	private Creature creature;
	private volatile String name;
	private WeakReference<Player> playerObject;
	private int hash = 0;
	private TagAttachment attachment;
	private Map<String, String> tags;
	private PersonaType type;
    private ArcheSkin skin;
    private ArrayList<PotionEffect> effects = Lists.newArrayList();

    ArchePersona(int persona_id, UUID player, int slot, String name, Race race, String gender, Timestamp creationTimeMS) {
        this(persona_id, player, slot, name, race, gender, creationTimeMS, PersonaType.NORMAL);
	}

    ArchePersona(int persona_id, UUID player, int slot, String name, Race race, String gender, Timestamp creationTimeMS, PersonaType type) {
        this.key = new ArchePersonaKey(player, slot);

		this.persona_id = persona_id;
		this.race = race;
		this.name = name;
		this.gender = gender;
		this.creationTimeMS = creationTimeMS;
		this.type = type;

		timePlayed = new AtomicInteger();
		charactersSpoken = new AtomicInteger();
		lastRenamed = new Timestamp(0);
		pastPlayTime = 0;

		tags = Maps.newConcurrentMap();

		sqlCriteria = Maps.newConcurrentMap();
		sqlCriteria.put("persona_id", persona_id);
	}

	@Override
    public int getPersonaId() {
        return persona_id;
	}

	public void addSkill(ArcheSkill skill, FutureTask<SkillData> future){
		SkillAttachment attach = new SkillAttachment(skill, this, future);
		skills.addSkillAttachment(attach);
	}

	public PersonaSkills getPersonaSkills() {
		return skills;
	}

	public PersonaAttributes attributes() {
		return attributes;
	}
	
	public SkillAttachment getSkill(Skill skill){
		return skills.getSkill(skill);
	}

	public void setPlayerName(String name) {
		this.name = name;
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
	public PersonaType getPersonaType() {
		return type;
	}

	@Override
	public void setPersonaType(PersonaType type) {
		this.type = type;

		buffer.put(new UpdateTask(this, PersonaField.TYPE, type));
	}

	@Override
	public double withdraw(double amount, Transaction cause) {
        //buffer.put(new InsertEconomyLogTask(persona_id, cause, amount));
        ArcheCore.getControls().getEconomy().withdrawPersona(this, amount, cause);
        return money;
	}

	@Override
	public double deposit(double amount, Transaction cause){
        //buffer.put(new InsertEconomyLogTask(persona_id, cause, amount));
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
		buffer.put(new UpdateTask(this,PersonaField.SKILL_SELECTED, name));
	}


	@Override
	public int getId(){
		return key.getPersonaId();
	}

	@Override
	public boolean isCurrent(){
		return current;
	}

    @Override
    public PersonaMagics getMagics() {
        return magics;
    }

    void setCurrent(boolean current) {
        if (this.current != current) {

			this.current = current;

			buffer.put(new UpdateTask(this, PersonaField.CURRENT, current));

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
			}
		}
	}

	void loadSkills(){
		for(ArcheSkill s : ArcheSkillFactory.getSkills().values()){

			//Start loading this Persona's Skill data for this one particular skill
			SelectSkillTask task = new SelectSkillTask(this, s);
			FutureTask<SkillData> fut = task.getFuture();
			buffer.put(task);

			addSkill(s, fut);
		}
	}

    void loadAttributes() {

    }

	void loadTags() {
        Callable<TagAttachment> attachmentCallable = new TagAttachmentCallable(persona_id, ArcheCore.getSQLControls());
        Future<TagAttachment> ft = SaveHandler.getInstance().prepareCallable(attachmentCallable);
        try {
            attachment = ft.get(100, TimeUnit.MILLISECONDS);

		} catch (TimeoutException e) {
			ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "We timed out while trying to fetch the persona " + persona_id + "'s tags!", e);
			attachment = new TagAttachment(Maps.newConcurrentMap(), persona_id, false);
		} catch (Exception e) {
			ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "We threw an exception while trying to fetch the persona " + persona_id + "'s tags!", e);
			attachment = new TagAttachment(Maps.newConcurrentMap(), persona_id, false);
		}
	}

	void loadMagics() {
        String sql = "SELECT magic_fk,tier,last_advanced,teacher,learned,visible FROM persona_magics WHERE persona_id_fk=?";
        try {
            Connection conn = ArcheCore.getSQLControls().getConnection();
            if (!ArcheCore.usingSQLite()) {
                conn.setReadOnly(true);
            }
            PreparedStatement stat = conn.prepareStatement(sql);
            stat.setInt(1, persona_id);
            ResultSet rs = stat.executeQuery();
			while (rs.next()) {
				MagicData data = null;
				String magic = rs.getString("magic_fk");
				Optional<Magic> armagic = ArcheCore.getMagicControls().researchMagic(magic);
				if (armagic.isPresent()) {
					int tier = rs.getInt("tier");
					Timestamp last_advanced = rs.getTimestamp("last_advanced");
					Timestamp learned = rs.getTimestamp("learned");
                    Integer teacher = rs.getInt("teacher");
                    boolean visible = rs.getBoolean("visible");
                    data = new MagicData(armagic.get(), tier, visible, teacher != null && teacher > 0, (teacher), learned.toInstant().toEpochMilli(), last_advanced.toInstant().toEpochMilli());
                    magics.addMagicAttachment(new MagicAttachment(armagic.get(), persona_id, data));
                }
			}
			rs.close();
			stat.close();
            if (!ArcheCore.usingSQLite()) {
                conn.close();
            }
        } catch (SQLException e) {
			ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "An error occurred while loading " + player + "'s persona magics!!!", e);
		}
	}

	void createEmptyTags() {
		attachment = new TagAttachment(Maps.newConcurrentMap(), persona_id, true);
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
	public Optional<Future<MagicAttachment>> createAttachment(Magic m, int tier, Persona teacher, boolean visible) {
        if (magics.hasMagic(m)) {
            return Optional.empty();
		}
        MagicCreateCallable call = new MagicCreateCallable(persona_id, (ArcheMagic) m, tier, (teacher == null ? null : teacher.getPersonaId()), visible, ArcheCore.getSQLControls());
        Future<MagicAttachment> future = buffer.prepareCallable(call);
		try {
			MagicAttachment attach = future.get(200, TimeUnit.MILLISECONDS);

            magics.addMagicAttachment(attach);
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
		buffer.put(new UpdateTask(this,PersonaField.PREFIX, prefix));
	}

	@Override
	public boolean hasPrefix(){
		return prefix != null && !prefix.isEmpty();
	}

	@Override
	public void clearPrefix(){
		prefix = null;
		updateDisplayName(Bukkit.getPlayer(this.getPlayerUUID()));
		buffer.put(new UpdateTask(this,PersonaField.PREFIX, prefix));
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
	public int getTimePlayed(){
		return timePlayed.get();
	}

	/**
	 * Atomically adds minutes to the total amount of this Persona's playtime.
	 * @param timePlayed The amount of minutes to add
	 */
	public void addTimePlayed(int timePlayed){
		int val = this.timePlayed.addAndGet(timePlayed);

		buffer.put(new UpdateTask(this, PersonaField.STAT_PLAYED, val));
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

		buffer.put(new UpdateTask(this, PersonaField.STAT_CHARS, val));
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
            sb.append(raceHeader).append(" ");
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
	public String getName(){
		return name;
	}

	@Override
	public void setName(String name) {
		PersonaRenameEvent event = new PersonaRenameEvent(this, name);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;

		this.name = name;
		lastRenamed = new Timestamp(System.currentTimeMillis());

		buffer.put(new UpdateTask(this, PersonaField.NAME, name));
		buffer.put(new UpdateTask(this, PersonaField.STAT_RENAMED, lastRenamed));

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
		buffer.put(new UpdateTask(this, PersonaField.RACE_REAL, race));
		buffer.put(new UpdateTask(this, PersonaField.RACE, ""));
		this.raceHeader = null;
	}

	@Override
	public void setApparentRace(String race){
		raceHeader = race;

		buffer.put(new UpdateTask(this, PersonaField.RACE, race));
	}

	@Override
	public Timestamp getRenamed() {
		return lastRenamed;
	}

	@Override
	public void clearDescription(){
		description = null;
		buffer.put(new UpdateTask(this, PersonaField.DESCRIPTION, null));
	}

	@Override
	public void addDescription(String addendum){
		if(description == null) description = addendum;
		else description = description + " " + addendum;

		buffer.put(new UpdateTask(this, PersonaField.DESCRIPTION, description));
	}

	@Override
	public String getDescription(){
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;

		buffer.put(new UpdateTask(this, PersonaField.DESCRIPTION, description));
	}

	@Override
	public String getGender(){
        return gender;
    }

	@Override
	 public void setGender(String gender) {
        this.gender = gender;
        buffer.put(new UpdateTask(this, PersonaField.GENDER, gender));
    }

	void saveMinecraftSpecifics(final Player p){
		//Store and switch Persona-related specifics: Location and Inventory.
		food = p.getFoodLevel();
		health = p.getHealth();
        saturation = p.getSaturation();
        inv = PersonaInventory.store(p);
		location = new WeakBlock(p.getLocation());
        savePotionEffects(p);
        buffer.put(new UpdateVitalsTask(persona_id, p.getWorld().getUID(), location.getX(), location.getY(), location.getZ(), health, saturation, food, inv));
    }

    public void savePotionEffects(Player pl) {
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
        buffer.put(new UpdateTask(this, PersonaField.POTIONS, config.saveToString()));
    }

    void loadPotionsFromString(String contents) {
        YamlConfiguration potionconfig = new YamlConfiguration();

        try {
            potionconfig.loadFromString(contents);
            if (potionconfig.getKeys(false).contains("potions")) {
                @SuppressWarnings("unchecked")
                List<PotionEffect> result = potionconfig.getList("contents").stream()
                        .map(ent -> (Map<String, Object>) ent)
                        .map(PotionEffect::new)
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
            pinv.setArmorContents(new ItemStack[4]);
		}
        attributes.applyToPlayer();

		//Heal them so their Persona is fresh
		double maxHp = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		if (health == 0) {
			health = maxHp;
		}
		if (food == 0) {
			food = 20;
		}
		if (maxHp < health) p.setHealth(maxHp);
		else p.setHealth(health);
		p.setFoodLevel(food);
        p.setSaturation(saturation);
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
		if(event.isCancelled()) return false;

        buffer.put(new PersonaDeleteTask(this));
        //buffer.put(new DataTask(DataTask.DELETE, TABLE, null, sqlCriteria));
        //		Both could be commented out once cascading db is setup!
        //buffer.put(new DataTask(DataTask.DELETE, "persona_names", null, sqlCriteria));
        handler.deleteSkills(this);

		ArchePersona[] prs = handler.getAllPersonas(this.getPlayerUUID());
		prs[getId()] = null;

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
			Inventory binv = Bukkit.createInventory(this, 45, "Persona Inventory: " + player + "@" + getId());
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
            Inventory einv = Bukkit.createInventory(this, 27, "Persona Enderchest: " + player + "@" + getId());
            einv.setContents(inv.getEnderContents());
            return einv;
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
		return this.persona_id == p.persona_id;
	}
	//
	@Override
	public boolean isNewbie() {
		return getTimePlayed() < ArcheCore.getControls().getNewbieDelay();
	}

	@Override
	public Timestamp getCreationTime() {
		return this.creationTimeMS;
	}

	@Override
	public int getTotalPlaytime(){
		return pastPlayTime + getTimePlayed();
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
			buffer.put(new UpdateFatigueTask(fatigue, persona_id, player));
		}
	}

    @Override
    public void setSkin(ArcheSkin skin) {
        this.skin = skin;
        skin.addPersona(this);
        buffer.put(new UpdateTask(this, PersonaField.ICON, skin.getSkinId()));
    }

    @Override
    public void removeSkin() {
        skin.removePersona(this);
        this.skin = null;
        buffer.put(new UpdateTask(this, PersonaField.ICON, -1));
    }

    @Override
    public ArcheSkin getSkin() {
        return skin;
    }

    @Override
    public boolean hasSkin() {
        return skin != null;
    }
}