package net.lordofthecraft.arche.persona;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.enums.AbilityScore;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.persona.PersonaFatigueEvent;
import net.lordofthecraft.arche.event.persona.PersonaRemoveEvent;
import net.lordofthecraft.arche.event.persona.PersonaRenameEvent;
import net.lordofthecraft.arche.event.persona.PersonaSwitchEvent;
import net.lordofthecraft.arche.interfaces.*;
import net.lordofthecraft.arche.listener.NewbieProtectListener;
import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.magic.MagicData;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.rows.magic.insert.MagicInsertRow;
import net.lordofthecraft.arche.save.rows.persona.delete.DeletePersonaSkinRow;
import net.lordofthecraft.arche.save.rows.persona.delete.PersonaDeleteRow;
import net.lordofthecraft.arche.save.rows.persona.insert.SkinRow;
import net.lordofthecraft.arche.save.rows.persona.update.PersonaUpdateRow;
import net.lordofthecraft.arche.save.rows.persona.update.VitalsUpdateRow;
import net.lordofthecraft.arche.save.tasks.skills.SelectSkillTask;
import net.lordofthecraft.arche.skill.ArcheSkill;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import net.lordofthecraft.arche.skill.SkillData;
import net.lordofthecraft.arche.skin.ArcheSkin;
import net.lordofthecraft.arche.skin.SkinCache;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.WeakBlock;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import java.lang.ref.WeakReference;
import java.sql.*;
import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class ArchePersona extends ArcheOfflinePersona implements Persona, InventoryHolder {

	private static final ArchePersonaHandler handler = ArchePersonaHandler.getInstance();
    //private static final ArcheExecutor buffer = ArcheExecutor.getInstance();

	final PersonaSkills skills = new PersonaSkills(this);
    final PersonaMagics magics = new PersonaMagics(this);
    final PersonaAttributes attributes = new PersonaAttributes(this);

	final Map<String,Object> sqlCriteria;
	final AtomicInteger charactersSpoken;
    String description = null;
	volatile String prefix = null;
	boolean current = true;
	String raceHeader = null;
	Timestamp lastRenamed;
	String player; //Last known minecraft name of the owning player
	WeakBlock location = null;
	double money = 0;
	double fatigue = 0;
	int food = 0;
    float saturation = 0;
    double health = 0;
    int unspent_points = 0;
    private Creature creature;
	private WeakReference<Player> playerObject;
	private TagAttachment attachment;
    private ArcheSkin skin;
    private ArrayList<PotionEffect> effects = Lists.newArrayList();

    ArchePersona(int persona_id, UUID player, int slot, String name, Race race, String gender, Timestamp creationTimeMS, Timestamp lastPlayed) {
        this(persona_id, player, slot, name, race, gender, creationTimeMS, lastPlayed, PersonaType.NORMAL);
    }

    ArchePersona(int persona_id, UUID player, int slot, String name, Race race, String gender, Timestamp creationTimeMS, Timestamp lastPlayed, PersonaType type) {
        //ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, Timestamp lastPlayed, boolean current, Race race, String gender, PersonaType type, String name) {
        super(new ArchePersonaKey(persona_id, player, slot), creationTimeMS, lastPlayed, false, race, gender, type, name);
        //this.key = new ArchePersonaKey(persona_id, player, slot);
		charactersSpoken = new AtomicInteger();
		lastRenamed = new Timestamp(0);
		pastPlayTime = 0;

		sqlCriteria = Maps.newConcurrentMap();
		sqlCriteria.put("persona_id", persona_id);
	}

    ArchePersona(ArcheOfflinePersona persona) {
        super(persona.personaKey, persona.creation, persona.lastPlayed, persona.current, persona.race, persona.gender, persona.type, persona.name);
        player = persona.getPlayerName();
        charactersSpoken = new AtomicInteger();
        lastRenamed = new Timestamp(0);
        pastPlayTime = 0;

        sqlCriteria = Maps.newConcurrentMap();
        sqlCriteria.put("persona_id", persona.getPersonaId());
    }

	@Override
    public int getPersonaId() {
        return personaKey.getPersonaID();
    }

	public void addSkill(ArcheSkill skill, FutureTask<SkillData> future){
		SkillAttachment attach = new SkillAttachment(skill, this, future);
		skills.addSkillAttachment(attach);
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
	public boolean hasTagKey(String s) {
        return attachment.hasKey(s);
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
        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.SKILL_SELECTED, name, false));
        //buffer.put(new UpdateTask(this,PersonaField.SKILL_SELECTED, name));
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

    @Override
    public int getScore(AbilityScore score) {
        return attributes.getInstance(AttributeRegistry.getSAttribute(score.getName()))
                .getModifiers()
                .stream()
                .filter(m -> m.getUniqueId().equals(PersonaHandler.SCORE_ID) || m.getUniqueId().equals(RaceBonusHandler.UUID_RACIAL_SCORE))
                .map(AttributeModifier::getAmount)
                .map(Double::new)
                .mapToInt(Double::intValue)
                .sum();
    }

    void setCurrent(boolean current) {
        if (this.current != current) {

			this.current = current;
            consumer.queueRow(new PersonaUpdateRow(this, PersonaField.CURRENT, this.current, false));
            //buffer.put(new UpdateTask(this, PersonaField.CURRENT, current));

			if (current) { // Persona becoming Player's current Persona.
				Player p = Bukkit.getPlayer(getPlayerUUID());
				if (p != null) {
					updateDisplayName(p);

					//Apply Racial bonuses
					if (ArcheCore.getControls().areRacialBonusesEnabled())
                        RaceBonusHandler.apply(this, race);


				} else {
					ArcheCore.getPlugin().getLogger().info("Player " + player + " was not found (null) as her Persona was switched.");
				}
			}
		}
	}

    void loadSkin(Connection connection) {
        String sql = "SELECT skin_id_fk FROM per_persona_skins WHERE persona_id_fk=?";
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, personaKey.getPersonaID());
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                ArcheSkin skin = SkinCache.getInstance().getSkinByID(rs.getInt("skin_id_fk"));
                if (skin != null) {
                    this.skin = skin;
                } else {
                    consumer.queueRow(new DeletePersonaSkinRow(this));
                }
            }
            rs.close();
            statement.close();
        } catch (SQLException ex) {
            ArcheCore.getPlugin().getLogger().log(Level.WARNING, "Failed to create skin for persona " + MessageUtil.identifyPersona(this), ex);
        }
    }

	void loadSkills(){
        //TODO no lazy loading.
        for(ArcheSkill s : ArcheSkillFactory.getSkills().values()){

			//Start loading this Persona's Skill data for this one particular skill
			SelectSkillTask task = new SelectSkillTask(this, s);
			FutureTask<SkillData> fut = task.getFuture();

            //buffer.put(task);

			addSkill(s, fut);
		}
	}

    void loadAttributes(Connection conn) {
        try {
            if (!ArcheCore.usingSQLite()) {
                conn.setReadOnly(true);
            }
            PreparedStatement statement = conn.prepareStatement("SELECT mod_uuid,attribute_type,mod_name,mod_value,operation,decayticks,decaytype,lostondeath FROM persona_attributes WHERE persona_id_fk=?");
            statement.setInt(1, personaKey.getPersonaID());
            ResultSet rs = statement.executeQuery();
            AttributeRegistry reg = AttributeRegistry.getInstance();
            while (rs.next()) {
                ArcheAttribute att = reg.getAttribute(rs.getString("attribute_type"));
                String type = rs.getString("decaytype");
                String sop = rs.getString("operation");
                AttributeModifier.Operation op = null;
                ExtendedAttributeModifier.Decay decaytype = null;
                for (ExtendedAttributeModifier.Decay at : ExtendedAttributeModifier.Decay.values()) {
                	if (at.name().equalsIgnoreCase(type)) {
                		decaytype = at;
                		break;
                	}
                }
                for (AttributeModifier.Operation fop : AttributeModifier.Operation.values()) {
                	if (fop.name().equalsIgnoreCase(sop)) {
                		op = fop;
                		break;
                	}
                }
                if (decaytype != null && op != null) {
                	UUID id = UUID.fromString(rs.getString("mod_uuid"));
                    String name = rs.getString("mod_name");
                    double amount = rs.getDouble("mod_value");
                	long ticks = rs.getLong("decayticks");
                	boolean lostondeath = rs.getBoolean("lostondeath");
                	attributes.addModifierFromSQL(att, new ExtendedAttributeModifier(id, name, amount, op, decaytype, ticks, lostondeath));
                }
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "Warning! We failed to load attributes for the persona of " + player + "! [" + getPersonaId() + "]", e);
        }
    }

    void loadTags(Connection conn) {
        String sql = "SELECT tag_key,tag_value FROM persona_tags WHERE persona_id_fk=?";
        try {
            PreparedStatement stat = conn.prepareStatement(sql);
            stat.setInt(1, getPersonaId());
            ResultSet rs = stat.executeQuery();
            Map<String, String> tags = Maps.newHashMap();
            while (rs.next()) {
                tags.putIfAbsent(rs.getString("tag_key"), rs.getString("tag_value"));
            }
            attachment = new TagAttachment(tags, this, true);
            rs.close();
            stat.close();
        } catch (SQLException e) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "Failed to retrieve tags for the persona " + MessageUtil.identifyPersona(this), e);
            attachment = new TagAttachment(Maps.newHashMap(), this, false);
        }
    }

    void loadMagics(Connection conn) {
        String sql = "SELECT magic_fk,tier,last_advanced,teacher,learned,visible FROM persona_magics WHERE persona_id_fk=?";
        try {
            if (!ArcheCore.usingSQLite()) {
                conn.setReadOnly(true);
            }
            PreparedStatement stat = conn.prepareStatement(sql);
            stat.setInt(1, getPersonaId());
            ResultSet rs = stat.executeQuery();
			while (rs.next()) {
				MagicData data = null;
				String magic = rs.getString("magic_fk");
                Optional<Magic> armagic = ArcheCore.getMagicControls().getMagic(magic);
                if (armagic.isPresent()) {
					int tier = rs.getInt("tier");
					Timestamp last_advanced = rs.getTimestamp("last_advanced");
					Timestamp learned = rs.getTimestamp("learned");
                    Integer teacher = rs.getInt("teacher");
                    boolean visible = rs.getBoolean("visible");
                    data = new MagicData(armagic.get(), tier, visible, teacher != null && teacher > 0, (teacher), learned.toInstant().toEpochMilli(), last_advanced.toInstant().toEpochMilli());
                    magics.addMagicAttachment(new MagicAttachment(armagic.get(), this, data));
                }
			}
			rs.close();
			stat.close();
        } catch (SQLException e) {
			ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "An error occurred while loading " + player + "'s persona magics!!!", e);
		}
	}

	void createEmptyTags() {
        attachment = new TagAttachment(Maps.newConcurrentMap(), this, true);
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
		updateDisplayName(Bukkit.getPlayer(this.getPlayerUUID()));
        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.PREFIX, prefix, false));
        //buffer.put(new UpdateTask(this,PersonaField.PREFIX, prefix));
    }

	@Override
	public boolean hasPrefix(){
		return prefix != null && !prefix.isEmpty();
	}

	@Override
	public void clearPrefix(){
		prefix = null;
		updateDisplayName(Bukkit.getPlayer(this.getPlayerUUID()));
        //buffer.put(new UpdateTask(this,PersonaField.PREFIX, prefix));
        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.PREFIX, prefix, false));
    }

	void updateDisplayName(Player p){
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

        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.STAT_PLAYED, val, false));
        //buffer.put(new UpdateTask(this, PersonaField.STAT_PLAYED, val));
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

        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.STAT_CHARS, val, false));
        //buffer.put(new UpdateTask(this, PersonaField.STAT_CHARS, val));
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

        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.NAME, name, false));
        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.STAT_RENAMED, lastRenamed, false));

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
                RaceBonusHandler.apply(this, race);
                p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			}
		}
        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.RACE_REAL, race.name(), false));
        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.RACE, "", false));
        this.raceHeader = null;
	}

	@Override
	public void setApparentRace(String race){
		raceHeader = race;

        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.RACE, race, false));
        //buffer.put(new UpdateTask(this, PersonaField.RACE, race));
    }

	@Override
	public Timestamp getRenamed() {
		return lastRenamed;
	}

	@Override
	public void clearDescription(){
		description = null;
        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.DESCRIPTION, null, false));
        //buffer.put(new UpdateTask(this, PersonaField.DESCRIPTION, null));
    }

	@Override
	public void addDescription(String addendum){
		if(description == null) description = addendum;
		else description = description + " " + addendum;

        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.DESCRIPTION, description, false));
        //buffer.put(new UpdateTask(this, PersonaField.DESCRIPTION, description));
    }

	@Override
	public String getDescription(){
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;

        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.DESCRIPTION, description, false));
        //buffer.put(new UpdateTask(this, PersonaField.DESCRIPTION, description));
    }

	void saveMinecraftSpecifics(final Player p){
		//Store and switch Persona-related specifics: Location and Inventory.
		food = p.getFoodLevel();
		health = p.getHealth();
        saturation = p.getSaturation();
        inv = PersonaInventory.store(p);
		location = new WeakBlock(p.getLocation());
        String pots = savePotionEffects(p);
        ArcheCore.getPlugin().getLogger().info("Player world is " + p.getWorld().getName() + " which has a UID of " + p.getWorld().getUID().toString());
        consumer.queueRow(new VitalsUpdateRow(this, p.getWorld().getUID(), location.getX(), location.getY(), location.getZ(), health, saturation, food, inv, pots));
        //buffer.put(new UpdateVitalsTask(persona_id, p.getWorld().getUID(), location.getX(), location.getY(), location.getZ(), health, saturation, food, inv, pots));
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
            pinv.setArmorContents(new ItemStack[4]);
		}
        attributes.handleSwitch(false);

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
		if(event.isCancelled()) return false;

        consumer.queueRow(new PersonaDeleteRow(this));
        //buffer.put(new PersonaDeleteTask(this));
        handler.deleteSkills(this);

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

	public Location getLocation(){
		if(location == null) return null;
		else return location.toLocation();
	}


    @Override
    public int hashCode() {
        return personaKey.getPersonaID();
    }

    @Override
    public boolean equals(Object object) {
        if(object == null) return false;
		if(!(object instanceof ArchePersona)) return false;
		ArchePersona p = (ArchePersona) object;
        return this.getPersonaId() == p.getPersonaId();
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
            consumer.queueRow(new PersonaUpdateRow(this, PersonaField.FATIGUE, fatigue, false));
            //buffer.put(new UpdateFatigueTask(fatigue, persona_id, player));
        }
	}

    @Override
    public void setSkin(ArcheSkin skin) {
        boolean update = this.skin != null;
        this.skin = skin;
        skin.addPersona(this);

        if (update) {
            consumer.queueRow(new PersonaUpdateRow(this, PersonaField.ICON, skin.getSkinId(), false));
        } else {
            consumer.queueRow(new SkinRow(this));
        }
        //buffer.put(new UpdateTask(this, PersonaField.ICON, skin.getSkinId()));
    }

    @Override
    public void removeSkin() {
        skin.removePersona(this);
        this.skin = null;

        consumer.queueRow(new DeletePersonaSkinRow(this));
        //buffer.put(new UpdateTask(this, PersonaField.ICON, -1));
    }

    @Override
    public OfflinePersona unloadPersona() {
        //super(persona.personaKey, persona.creation, persona.lastPlayed, persona.current, persona.race, persona.gender, persona.type, persona.name);
        return new ArcheOfflinePersona(personaKey, creation, lastPlayed, current, race, gender, type, name);
    }

    @Override
    public void setUnspentPoints(int points) {
        if (points < 0) points = 0;
        this.unspent_points = points;
        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.POINTS, unspent_points, false));
    }

    @Override
    public int getUnspentPoints() {
        return unspent_points;
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
    public Persona getPersona() {
        return this;
    }

    @Override
    public Persona loadPersona(ResultSet rs) {
        return this;
    }
}