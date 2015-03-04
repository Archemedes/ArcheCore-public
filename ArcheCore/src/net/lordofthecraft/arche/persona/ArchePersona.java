package net.lordofthecraft.arche.persona;

import java.util.concurrent.atomic.*;
import java.lang.ref.*;
import org.bukkit.entity.*;
import com.google.common.collect.*;
import java.util.concurrent.*;
import net.lordofthecraft.arche.*;
import java.util.logging.*;
import net.lordofthecraft.arche.save.*;
import net.lordofthecraft.arche.enums.*;
import net.lordofthecraft.arche.skill.*;
import net.lordofthecraft.arche.interfaces.*;
import java.util.*;
import org.bukkit.event.*;
import net.lordofthecraft.arche.listener.*;
import org.bukkit.scheduler.*;
import org.bukkit.inventory.*;
import org.apache.commons.lang.*;
import net.lordofthecraft.arche.save.tasks.*;
import net.lordofthecraft.arche.event.*;
import org.bukkit.plugin.*;
import org.bukkit.*;

public final class ArchePersona implements Persona
{
    private static final String TABLE = "persona";
    private static final ArchePersonaHandler handler;
    private static final SaveHandler buffer;
    final Map<String, Object> sqlCriteria;
    private final ArchePersonaKey key;
    private final Race race;
    private final int gender;
    int age;
    private volatile String name;
    String description;
    volatile String prefix;
    boolean current;
    boolean autoAge;
    String raceHeader;
    final AtomicInteger timePlayed;
    final AtomicInteger charactersSpoken;
    long lastRenamed;
    String player;
    private WeakReference<Player> playerObject;
    WeakBlock location;
    PersonaInventory inv;
    double money;
    private int hash;
    private final List<SkillAttachment> profs;
    boolean gainsXP;
    Skill profession;
    Skill professionPrimary;
    Skill professionSecondary;
    Skill professionAdditional;
    
    ArchePersona(final OfflinePlayer p, final int id, final String name, final Race race, final int gender, final int age) {
        super();
        this.description = null;
        this.prefix = null;
        this.current = true;
        this.raceHeader = null;
        this.location = null;
        this.inv = null;
        this.money = 0.0;
        this.hash = 0;
        this.profs = Lists.newArrayList();
        this.gainsXP = true;
        this.profession = null;
        this.professionPrimary = null;
        this.professionSecondary = null;
        this.professionAdditional = null;
        this.key = new ArchePersonaKey(p.getUniqueId(), id);
        this.player = p.getName();
        this.race = race;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.timePlayed = new AtomicInteger();
        this.charactersSpoken = new AtomicInteger();
        this.lastRenamed = 0L;
        (this.sqlCriteria = Maps.newHashMap()).put("player", this.getPlayerUUID().toString());
        this.sqlCriteria.put("id", id);
    }
    
    public void addSkill(final ArcheSkill skill, final FutureTask<SkillData> future) {
        if (this.profs.size() != skill.getId()) {
            final Logger log = ArcheCore.getPlugin().getLogger();
            log.severe("Incorrect skill ordering in Persona LinkedList!");
            log.severe("Expect length " + skill.getId() + " but got " + this.profs.size() + " for Persona: " + this.player + "_" + this.getId());
            log.severe("Will see errors and incorrect xp assignments of skills!");
        }
        final SkillAttachment attach = new SkillAttachment(skill, this, future);
        this.profs.add(attach);
    }
    
    public SkillAttachment getSkill(final int skillId) {
        return this.profs.get(skillId);
    }
    
    @Override
    public Skill getMainSkill() {
        return this.profession;
    }
    
    @Override
    public void setMainSkill(final Skill profession) {
        this.profession = profession;
        final String name = (profession == null) ? null : profession.getName();
        ArchePersona.buffer.put(new UpdateTask(this, PersonaField.SKILL_SELECTED, name));
    }
    
    @Override
    public Skill getProfession(final ProfessionSlot slot) {
        switch (slot) {
            case PRIMARY: {
                return this.professionPrimary;
            }
            case SECONDARY: {
                return this.professionSecondary;
            }
            case ADDITIONAL: {
                return this.professionAdditional;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
    }
    
    @Override
    public void setProfession(final ProfessionSlot slot, final Skill profession) {
        switch (slot) {
            case PRIMARY: {
                this.professionPrimary = profession;
                break;
            }
            case SECONDARY: {
                this.professionSecondary = profession;
                break;
            }
            case ADDITIONAL: {
                this.professionAdditional = profession;
                break;
            }
            default: {
                throw new IllegalArgumentException();
            }
        }
        final String name = (profession == null) ? null : profession.getName();
        ArchePersona.buffer.put(new UpdateTask(this, slot.getPersonaField(), name));
    }
    
    public void handleProfessionSelection() {
        for (final SkillAttachment att : this.profs) {
            if (!att.isInitialized()) {
                att.initialize();
            }
            final SkillTier tier = att.skill.getCapTier(this);
            if (att.getXp() > tier.getXp()) {
                att.setXp(tier.getXp());
            }
            else {
                if (att.getXp() >= 0.0 || tier == SkillTier.RUSTY) {
                    continue;
                }
                att.setXp(0.0);
            }
        }
    }
    
    @Override
    public int getId() {
        return this.key.getPersonaId();
    }
    
    @Override
    public boolean isCurrent() {
        return this.current;
    }
    
    void loadSkills() {
        for (final ArcheSkill s : ArcheSkillFactory.getSkills().values()) {
            final SelectSkillTask task = new SelectSkillTask(this, s);
            final FutureTask<SkillData> fut = task.getFuture();
            ArchePersona.buffer.put(task);
            this.addSkill(s, fut);
        }
    }
    
    @Override
    public void setPrefix(final String prefix) {
        this.prefix = prefix;
        this.updateDisplayName(Bukkit.getPlayer(this.getPlayerUUID()));
        ArchePersona.buffer.put(new UpdateTask(this, PersonaField.PREFIX, prefix));
    }
    
    @Override
    public String getPrefix() {
        return this.prefix;
    }
    
    @Override
    public boolean hasPrefix() {
        return this.prefix != null && !this.prefix.isEmpty();
    }
    
    @Override
    public void clearPrefix() {
        this.prefix = null;
        this.updateDisplayName(Bukkit.getPlayer(this.getPlayerUUID()));
        ArchePersona.buffer.put(new UpdateTask(this, PersonaField.PREFIX, this.prefix));
    }
    
    void updateDisplayName(final Player p) {
        if (ArchePersona.handler.willModifyDisplayNames() && p != null) {
            if (this.hasPrefix() && ArcheCore.getPlugin().arePrefixesEnabled()) {
                p.setDisplayName("[" + this.getPrefix() + "] " + this.name);
            }
            else {
                p.setDisplayName(this.name);
            }
        }
    }
    
    void setCurrent(final boolean current) {
        if (this.current != current) {
            this.current = current;
            ArchePersona.buffer.put(new UpdateTask(this, PersonaField.CURRENT, current));
            if (current) {
                final Player p = Bukkit.getPlayer(this.getPlayerUUID());
                if (p != null) {
                    this.updateDisplayName(p);
                    if (ArcheCore.getControls().areRacialBonusesEnabled()) {
                        RaceBonusHandler.apply(p, this.race);
                    }
                }
                else {
                    ArcheCore.getPlugin().getLogger().info("Player " + this.player + " was not found (null) as her Persona was switched.");
                }
            }
        }
    }
    
    @Override
    public void setXPGain(final boolean gainsXP) {
        if (this.gainsXP != gainsXP) {
            this.gainsXP = gainsXP;
            ArchePersona.buffer.put(new UpdateTask(this, PersonaField.XP_GAIN, gainsXP));
        }
    }
    
    @Override
    public boolean getXPGain() {
        return this.gainsXP;
    }
    
    @Override
    public int getTimePlayed() {
        return this.timePlayed.get();
    }
    
    public void addTimePlayed(final int timePlayed) {
        final int val = this.timePlayed.addAndGet(timePlayed);
        ArchePersona.buffer.put(new UpdateTask(this, PersonaField.STAT_PLAYED, val));
    }
    
    @Override
    public int getCharactersSpoken() {
        return this.charactersSpoken.get();
    }
    
    public void addCharactersSpoken(final int charsSpoken) {
        final int val = this.charactersSpoken.addAndGet(charsSpoken);
        ArchePersona.buffer.put(new UpdateTask(this, PersonaField.STAT_CHARS, val));
    }
    
    @Override
    public String getPlayerName() {
        return this.player;
    }
    
    @Override
    public PersonaKey getPersonaKey() {
        return this.key;
    }
    
    @Override
    public Player getPlayer() {
        Player play;
        if (this.playerObject == null || (play = this.playerObject.get()) == null || play.isDead()) {
            play = Bukkit.getPlayer(this.getPlayerUUID());
            if (play == null) {
                this.playerObject = null;
                return null;
            }
            this.playerObject = new WeakReference<Player>(play);
        }
        return play;
    }
    
    @Override
    public UUID getPlayerUUID() {
        return this.key.getPlayerUUID();
    }
    
    @Override
    public String getChatName() {
        if (this.prefix == null || this.prefix.isEmpty()) {
            return this.getName();
        }
        return "[" + this.getPrefix() + "] " + this.getName();
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public Race getRace() {
        return this.race;
    }
    
    @Override
    public String getRaceString() {
        if (this.raceHeader == null || this.raceHeader.isEmpty()) {
            return this.race.getName();
        }
        return this.raceHeader;
    }
    
    @Override
    public void setApparentRace(final String race) {
        this.raceHeader = race;
        ArchePersona.buffer.put(new UpdateTask(this, PersonaField.RACE, race));
    }
    
    @Override
    public long getRenamed() {
        return this.lastRenamed;
    }
    
    @Override
    public void setName(final String name) {
        final PersonaRenameEvent event = new PersonaRenameEvent(this, name);
        Bukkit.getPluginManager().callEvent((Event)event);
        if (event.isCancelled()) {
            return;
        }
        this.name = name;
        this.lastRenamed = System.currentTimeMillis();
        ArchePersona.buffer.put(new UpdateTask(this, PersonaField.NAME, name));
        ArchePersona.buffer.put(new UpdateTask(this, PersonaField.STAT_RENAMED, this.lastRenamed));
        if (this.current) {
            final Player p = Bukkit.getPlayer(this.getPlayerUUID());
            this.updateDisplayName(p);
        }
    }
    
    @Override
    public void clearDescription() {
        this.description = null;
        ArchePersona.buffer.put(new UpdateTask(this, PersonaField.DESCRIPTION, this.description));
    }
    
    @Override
    public void setDescription(final String description) {
        this.description = description;
        ArchePersona.buffer.put(new UpdateTask(this, PersonaField.DESCRIPTION, description));
    }
    
    @Override
    public void addDescription(final String addendum) {
        if (this.description == null) {
            this.description = addendum;
        }
        else {
            this.description = this.description + " " + addendum;
        }
        ArchePersona.buffer.put(new UpdateTask(this, PersonaField.DESCRIPTION, this.description));
    }
    
    @Override
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public String getGender() {
        switch (this.gender) {
            case 0: {
                return "Female";
            }
            case 1: {
                return "Male";
            }
            default: {
                return null;
            }
        }
    }
    
    @Override
    public int getAge() {
        return this.age;
    }
    
    @Override
    public void setAge(final int age) {
        this.age = age;
        ArchePersona.buffer.put(new UpdateTask(this, PersonaField.AGE, age));
    }
    
    @Override
    public boolean doesAutoAge() {
        return this.autoAge;
    }
    
    @Override
    public void setAutoAge(final boolean autoAge) {
        this.autoAge = autoAge;
        ArchePersona.buffer.put(new UpdateTask(this, PersonaField.AUTOAGE, autoAge));
    }
    
    void saveMinecraftSpecifics(final Player p) {
        this.inv = PersonaInventory.store(p);
        this.location = new WeakBlock(p.getLocation());
        ArchePersona.buffer.put(new PersonaSwitchTask(this));
    }
    
    void restoreMinecraftSpecifics(final Player p) {
        if (this.location != null) {
            p.teleport(this.location.toLocation().add(0.5, 0.5, 0.5));
        }
        if (ArcheCore.getPlugin().teleportProtectively()) {
            NewbieProtectListener.bonusProtects.add(p.getUniqueId());
            new BukkitRunnable() {
                public void run() {
                    NewbieProtectListener.bonusProtects.remove(p.getUniqueId());
                }
            };
        }
        final PlayerInventory pinv = p.getInventory();
        if (this.inv != null) {
            pinv.setContents(this.inv.getContents());
            pinv.setArmorContents(this.inv.getArmorContents());
            this.inv = null;
        }
        else {
            pinv.clear();
            pinv.setArmorContents(new ItemStack[4]);
        }
        p.setHealth(p.getMaxHealth());
        p.setFoodLevel(20);
    }
    
    @Override
    public boolean remove() {
        final Player p = Bukkit.getPlayer(this.getPlayerUUID());
        Validate.notNull((Object)p);
        final PersonaRemoveEvent event = new PersonaRemoveEvent(this, false);
        Bukkit.getPluginManager().callEvent((Event)event);
        if (event.isCancelled()) {
            return false;
        }
        ArchePersona.buffer.put(new DataTask(4, "persona", null, this.sqlCriteria));
        ArchePersona.buffer.put(new DataTask(4, "persona_names", null, this.sqlCriteria));
        ArchePersona.handler.deleteSkills(this);
        final ArchePersona[] prs = ArchePersona.handler.getAllPersonas(this.getPlayerUUID());
        prs[this.getId()] = null;
        if (this.isCurrent()) {
            boolean success = false;
            for (int i = 0; i < prs.length; ++i) {
                if (prs[i] != null) {
                    final PersonaSwitchEvent ev2 = new PersonaSwitchEvent(prs[i]);
                    Bukkit.getPluginManager().callEvent((Event)ev2);
                    if (!ev2.isCancelled()) {
                        prs[i].setCurrent(true);
                        prs[i].restoreMinecraftSpecifics(p);
                        success = true;
                        break;
                    }
                }
            }
            if (!success) {
                final Plugin plugin = (Plugin)ArcheCore.getPlugin();
                plugin.getLogger().warning("Player " + this.player + " removed his final usable Persona!");
                RaceBonusHandler.reset(p);
                if (p.hasPermission("archecore.mayuse") && !p.hasPermission("archecore.exempt")) {
                    new CreationDialog().makeFirstPersona(p);
                }
            }
            p.sendMessage(ChatColor.DARK_PURPLE + "Your persona was removed: " + ChatColor.GRAY + this.getName());
        }
        return true;
    }
    
    public PersonaInventory getInventory() {
        return this.inv;
    }
    
    public Location getLocation() {
        if (this.location == null) {
            return null;
        }
        return this.location.toLocation();
    }
    
    @Override
    public int hashCode() {
        if (this.hash == 0) {
            this.hash = 5 * this.player.hashCode() + this.getId();
        }
        return this.hash;
    }
    
    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof ArchePersona)) {
            return false;
        }
        final ArchePersona p = (ArchePersona)object;
        return this.player.equals(p.player) && this.getId() == p.getId();
    }
    
    static {
        handler = ArchePersonaHandler.getInstance();
        buffer = SaveHandler.getInstance();
    }
}
