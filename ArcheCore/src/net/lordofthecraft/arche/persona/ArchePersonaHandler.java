package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.save.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import net.lordofthecraft.arche.enums.*;
import net.lordofthecraft.arche.event.*;
import java.util.concurrent.*;
import net.lordofthecraft.arche.skill.*;
import org.bukkit.potion.*;
import net.lordofthecraft.arche.save.tasks.*;
import java.util.*;
import org.bukkit.*;
import org.apache.commons.lang.*;
import net.lordofthecraft.arche.interfaces.*;
import java.sql.*;
import org.bukkit.plugin.*;
import net.lordofthecraft.arche.*;
import org.bukkit.configuration.*;
import com.google.common.collect.*;
import net.lordofthecraft.arche.SQL.*;

public class ArchePersonaHandler implements PersonaHandler
{
    private SaveHandler buffer;
    private boolean displayName;
    private static final ArchePersonaHandler instance;
    private final Map<UUID, ArchePersona[]> personas;
    private PreparedStatement selectStatement;
    
    public static ArchePersonaHandler getInstance() {
        return ArchePersonaHandler.instance;
    }
    
    private ArchePersonaHandler() {
        super();
        this.buffer = SaveHandler.getInstance();
        this.displayName = false;
        this.personas = new HashMap<UUID, ArchePersona[]>(Bukkit.getServer().getMaxPlayers());
        this.selectStatement = null;
    }
    
    @Override
    public void setModifyDisplayNames(final boolean will) {
        this.displayName = will;
    }
    
    @Override
    public boolean willModifyDisplayNames() {
        return this.displayName;
    }
    
    @Override
    public boolean mayUse(final Player p) {
        return p.hasPermission("archecore.mayuse");
    }
    
    @Override
    public int getAllowedPersonas(final Player p) {
        if (!this.mayUse(p)) {
            return 0;
        }
        for (int i = 4; i > 1; --i) {
            if (p.hasPermission("archecore.personas." + i)) {
                return i;
            }
        }
        return 1;
    }
    
    public Collection<ArchePersona[]> getPersonas() {
        return Collections.unmodifiableCollection((Collection<? extends ArchePersona[]>)this.personas.values());
    }
    
    @Override
    public ArchePersona getPersona(final Player p) {
        if (p == null) {
            return null;
        }
        final ArchePersona[] prs = this.personas.get(p.getUniqueId());
        if (prs == null) {
            return null;
        }
        for (int i = 0; i < prs.length; ++i) {
            if (prs[i] != null && prs[i].isCurrent()) {
                return prs[i];
            }
        }
        return null;
    }
    
    @Override
    public ArchePersona getPersona(final PersonaKey key) {
        if (key == null) {
            return null;
        }
        return this.getPersona(key.getPlayerUUID(), key.getPersonaId());
    }
    
    @Override
    public ArchePersona getPersona(final UUID uuid, final int id) {
        final ArchePersona[] prs = this.personas.get(uuid);
        if (prs != null) {
            return prs[id];
        }
        return null;
    }
    
    public ArchePersona getPersona(final UUID uuid) {
        final ArchePersona[] prs = this.personas.get(uuid);
        if (prs == null) {
            return null;
        }
        for (int i = 0; i < prs.length; ++i) {
            if (prs[i] != null && prs[i].isCurrent()) {
                return prs[i];
            }
        }
        return null;
    }
    
    @Override
    public ArchePersona getPersona(final OfflinePlayer p) {
        if (p == null) {
            return null;
        }
        return this.getPersona(p.getUniqueId());
    }
    
    @Override
    public boolean hasPersona(final Player p) {
        return this.getPersona(p) != null;
    }
    
    @Override
    public ArchePersona[] getAllPersonas(final OfflinePlayer p) {
        return this.getAllPersonas(p.getUniqueId());
    }
    
    @Override
    public ArchePersona[] getAllPersonas(final UUID uuid) {
        final ArchePersona[] prs = this.personas.get(uuid);
        if (prs == null) {
            return new ArchePersona[4];
        }
        return prs;
    }
    
    @Override
    public int countPersonas(final UUID uuid) {
        return this.countPersonas(this.getAllPersonas(uuid));
    }
    
    @Override
    public int countPersonas(final Player p) {
        return this.countPersonas(this.getAllPersonas((OfflinePlayer)p));
    }
    
    public void unload(final UUID uuid) {
        final Player p = Bukkit.getPlayer(uuid);
        if (p == null) {
            this.personas.remove(uuid);
        }
        if (ArcheCore.getPlugin().debugMode()) {
            ArcheCore.getPlugin().getLogger().info("[Debug] Unloaded player '" + uuid + "' who was null: " + (p == null) + ". Loaded players now " + this.personas.size());
        }
    }
    
    private int countPersonas(final ArchePersona[] prs) {
        int result = 0;
        for (int i = 0; i < prs.length; ++i) {
            if (prs[i] != null) {
                ++result;
            }
        }
        return result;
    }
    
    @Override
    public void switchPersona(final Player p, final int id) {
        if (id < 0 || id > 3) {
            throw new IllegalArgumentException("Only Persona IDs 0-3 are allowed.");
        }
        ArchePersona before = null;
        final ArchePersona[] prs = this.personas.get(p.getUniqueId());
        final ArchePersona after = prs[id];
        final PersonaSwitchEvent event = new PersonaSwitchEvent(prs[id]);
        Bukkit.getPluginManager().callEvent((Event)event);
        if (event.isCancelled()) {
            return;
        }
        for (int i = 0; i < prs.length; ++i) {
            if (prs[i] != null) {
                final boolean setAs = prs[i].getId() == id;
                if (before == null && prs[i].current && !setAs) {
                    before = prs[i];
                }
                prs[i].setCurrent(setAs);
            }
        }
        Bukkit.getPluginManager().callEvent((Event)new PersonaActivateEvent(after, PersonaActivateEvent.Reason.SWITCH));
        if (before != null) {
            Bukkit.getPluginManager().callEvent((Event)new PersonaDeactivateEvent(before, PersonaDeactivateEvent.Reason.SWITCH));
        }
        if (before != null && before != after) {
            before.saveMinecraftSpecifics(p);
        }
        after.restoreMinecraftSpecifics(p);
    }
    
    @Override
    public ArchePersona createPersona(final Player p, final int id, final String name, final Race race, final int gender, final int age, final boolean autoAge) {
        ArchePersona[] prs = this.personas.get(p.getUniqueId());
        if (prs == null) {
            prs = new ArchePersona[4];
            this.personas.put(p.getUniqueId(), prs);
        }
        if (prs[id] != null) {
            final PersonaRemoveEvent event2 = new PersonaRemoveEvent(prs[id], true);
            Bukkit.getPluginManager().callEvent((Event)event2);
            if (event2.isCancelled()) {
                return null;
            }
            this.buffer.put(new DataTask(4, "persona_names", null, prs[id].sqlCriteria));
            this.deleteSkills(prs[id]);
        }
        final ArchePersona persona = new ArchePersona((OfflinePlayer)p, id, name, race, gender, age);
        persona.autoAge = autoAge;
        final PersonaCreateEvent event2 = new PersonaCreateEvent(persona, prs[id]);
        Bukkit.getPluginManager().callEvent((Event)event2);
        if (prs[id] != null) {
            this.buffer.put(new DataTask(4, "persona_names", null, prs[id].sqlCriteria));
            this.deleteSkills(prs[id]);
        }
        prs[id] = persona;
        for (final ArcheSkill s : ArcheSkillFactory.getSkills().values()) {
            persona.addSkill(s, null);
        }
        final String uuid = p.getUniqueId().toString();
        for (final PotionEffect ps : p.getActivePotionEffects()) {
            p.removePotionEffect(ps.getType());
        }
        final ArcheTask task = new InsertTask(uuid, id, name, age, race, gender, autoAge);
        this.buffer.put(task);
        RaceBonusHandler.apply(p, race);
        persona.updateDisplayName(p);
        this.switchPersona(p, id);
        if (ArcheCore.getControls().teleportNewPersonas()) {
            final World w = ArcheCore.getControls().getNewPersonaWorld();
            final Location to = (w == null) ? p.getWorld().getSpawnLocation() : w.getSpawnLocation();
            p.teleport(to);
        }
        return persona;
    }
    
    @Override
    public List<String> whois(final Persona p) {
        final List<String> result = Lists.newArrayList();
        if (p == null) {
            return result;
        }
        final String r = ChatColor.RESET + "";
        final String c = ChatColor.BLUE + "";
        final String l = ChatColor.GRAY + "";
        result.add(l + "~~~~ " + r + p.getPlayerName() + "'s Roleplay Persona" + l + " ~~~~");
        if (p.getTimePlayed() < ArcheCore.getPlugin().getNewbieProtectDelay()) {
            final Player player = ArcheCore.getPlayer(p.getPlayerUUID());
            if (player != null && !player.hasPermission("archecore.persona.nonewbie")) {
                result.add(ChatColor.LIGHT_PURPLE + "((Persona was recently made and can't engage in PvP))");
            }
            else {
                result.add(ChatColor.DARK_RED + "((Please remember not to metagame this information))");
            }
        }
        else if (ArcheCore.getPlugin().getNewbieNotificationDelay() > 0 && p.getTimePlayed() < 600) {
            final Player player = ArcheCore.getPlayer(p.getPlayerUUID());
            final long age = (player == null) ? 2147483647L : (System.currentTimeMillis() - player.getFirstPlayed());
            final int mins = (int)(age / 60000L);
            if (ArcheCore.getPlugin().getNewbieNotificationDelay() > mins && !player.hasPermission("archecore.persona.nonewbie")) {
                result.add(ChatColor.AQUA + "((This player is new to the server))");
            }
            else {
                result.add(ChatColor.DARK_RED + "((Please remember not to metagame this information))");
            }
        }
        else {
            result.add(ChatColor.DARK_RED + "((Please remember not to metagame this information))");
        }
        result.add(c + "Name: " + r + p.getName());
        final String race = p.getRaceString();
        if (!race.equals("Unset")) {
            result.add(c + "Race: " + r + race);
        }
        final String gender = p.getGender();
        if (gender != null) {
            result.add(c + "Gender: " + r + p.getGender());
        }
        final boolean aa = p.doesAutoAge();
        if (p.getAge() > 0 || aa) {
            result.add((aa ? c : ChatColor.DARK_RED) + "Age: " + r + p.getAge());
        }
        final String desc = p.getDescription();
        if (desc != null) {
            result.add(c + "Description: " + r + desc);
        }
        final Skill prof = p.getMainSkill();
        if (prof != null) {
            final String title = prof.getSkillTier(p).getTitle();
            result.add(c + "Profession: " + r + title + " " + WordUtils.capitalize(prof.getName()));
        }
        return result;
    }
    
    @Override
    public List<String> whois(final Player p) {
        return this.whois(this.getPersona(p));
    }
    
    public void initPlayer(final Player p) {
        if (this.personas.containsKey(p.getUniqueId())) {
            final ArcheCore plug = ArcheCore.getPlugin();
            if (!plug.willCachePersonas()) {
                plug.getLogger().warning("Player " + p.getName() + " logged in while already being registered. Quick relog?");
                plug.getLogger().warning("Currently have " + this.personas.size() + " persona files for " + Bukkit.getOnlinePlayers().size() + " players.");
            }
            final ArchePersona[] array;
            final ArchePersona[] perses = array = this.personas.get(p.getUniqueId());
            for (final ArchePersona pers : array) {
                if (pers != null) {
                    pers.player = p.getName();
                }
            }
            final ArchePersona current = this.getPersona(p);
            if (current != null) {
                if (ArcheCore.getPlugin().areRacialBonusesEnabled()) {
                    RaceBonusHandler.apply(p, current.getRace());
                }
                else {
                    RaceBonusHandler.reset(p);
                }
                current.updateDisplayName(p);
            }
            else {
                if (plug.debugMode()) {
                    plug.getLogger().info("[DEBUG] Player " + p.getName() + " was preloaded and did not have current persona.");
                }
                this.ensureValidPersonaRecord(p, perses, false);
            }
            return;
        }
        final ArchePersona[] prs = new ArchePersona[4];
        final SQLHandler handler = ArcheCore.getPlugin().getSQLHandler();
        boolean hasCurrent = false;
        ResultSet res = null;
        try {
            if (this.selectStatement == null) {
                this.selectStatement = handler.getSQL().getConnection().prepareStatement("SELECT * FROM persona WHERE player = ?");
            }
            this.selectStatement.setString(1, p.getUniqueId().toString());
            res = this.selectStatement.executeQuery();
            while (res.next()) {
                final ArchePersona persona = this.buildPersona(res, (OfflinePlayer)p);
                prs[persona.getId()] = persona;
                if (persona.current) {
                    if (!hasCurrent) {
                        hasCurrent = true;
                        persona.updateDisplayName(p);
                        if (ArcheCore.getPlugin().areRacialBonusesEnabled()) {
                            RaceBonusHandler.apply(p, persona.getRace());
                        }
                        else {
                            RaceBonusHandler.reset(p);
                        }
                    }
                    else {
                        ArcheCore.getPlugin().getLogger().warning("Player " + p.getName() + " has simultaneous current Personas. Fixing now...");
                        persona.setCurrent(false);
                    }
                }
            }
        }
        catch (SQLException e1) {
            e1.printStackTrace();
        }
        finally {
            if (res != null) {
                try {
                    res.close();
                }
                catch (SQLException e2) {
                    e2.printStackTrace();
                }
            }
            this.ensureValidPersonaRecord(p, prs, hasCurrent);
            this.personas.put(p.getUniqueId(), prs);
        }
    }
    
    private void ensureValidPersonaRecord(final Player p, final ArchePersona[] prs, final boolean hasCurrent) {
        if (this.countPersonas(prs) == 0) {
            RaceBonusHandler.reset(p);
            if (p.hasPermission("archecore.mayuse")) {
                if (p.hasPermission("archecore.exempt")) {
                    if (p.hasPermission("archecore.command.beaconme")) {
                        p.sendMessage(ChatColor.LIGHT_PURPLE + "No Personas found. Maybe use " + ChatColor.ITALIC + "/beaconme");
                    }
                }
                else {
                    if (ArcheCore.getControls().teleportNewPersonas()) {
                        final World w = ArcheCore.getControls().getNewPersonaWorld();
                        final Location l = (w == null) ? p.getWorld().getSpawnLocation() : w.getSpawnLocation();
                        p.teleport(l);
                    }
                    Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)ArcheCore.getPlugin(), (Runnable)new Runnable() {
                        @Override
                        public void run() {
                            new CreationDialog().makeFirstPersona(p);
                        }
                    }, 30L);
                }
            }
        }
        else if (!hasCurrent) {
            ArcheCore.getPlugin().getLogger().warning("Player " + p.getName() + " logged in with no Persona set as current. Fixing now.");
            int i = 0;
            while (i < prs.length) {
                if (prs[i] != null) {
                    prs[i].setCurrent(true);
                    if (!ArcheCore.getPlugin().areRacialBonusesEnabled()) {
                        RaceBonusHandler.reset(p);
                        break;
                    }
                    break;
                }
                else {
                    ++i;
                }
            }
        }
    }
    
    private ArchePersona buildPersona(final ResultSet res, final OfflinePlayer p) throws SQLException {
        final int id = res.getInt(2);
        final String name = res.getString(3);
        final int age = res.getInt(4);
        final Race race = Race.valueOf(res.getString(5));
        final String rheader = res.getString(6);
        final int gender = res.getInt(7);
        final ArchePersona persona = new ArchePersona(p, id, name, race, gender, age);
        if (rheader != null && !rheader.equals("null") && !rheader.isEmpty()) {
            persona.raceHeader = rheader;
        }
        persona.description = res.getString(8);
        persona.prefix = res.getString(9);
        persona.current = res.getBoolean(10);
        persona.autoAge = res.getBoolean(11);
        persona.timePlayed.set(res.getInt(12));
        persona.charactersSpoken.set(res.getInt(13));
        persona.lastRenamed = res.getLong(14);
        persona.gainsXP = res.getBoolean(15);
        persona.profession = ArcheSkillFactory.getSkill(res.getString(16));
        final String wstr = res.getString(17);
        if (!res.wasNull()) {
            final UUID wuuid = UUID.fromString(wstr);
            final World w = Bukkit.getWorld(wuuid);
            if (w != null) {
                final int x = res.getInt(18);
                final int y = res.getInt(19);
                final int z = res.getInt(20);
                final WeakBlock loc = new WeakBlock(w, x, y, z);
                persona.location = loc;
            }
        }
        final String invString = res.getString(21);
        if (!res.wasNull()) {
            try {
                persona.inv = PersonaInventory.restore(invString);
            }
            catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
        if (ArcheCore.getControls().usesEconomy()) {
            persona.money = res.getDouble(22);
        }
        persona.professionPrimary = ArcheSkillFactory.getSkill(res.getString(23));
        persona.professionSecondary = ArcheSkillFactory.getSkill(res.getString(24));
        persona.professionAdditional = ArcheSkillFactory.getSkill(res.getString(25));
        persona.loadSkills();
        return persona;
    }
    
    public void initPreload(final int range) {
        final SQLHandler handler = ArcheCore.getPlugin().getSQLHandler();
        final long time = System.currentTimeMillis();
        try {
            final ResultSet res = handler.query("SELECT * FROM persona");
            while (res.next()) {
                final UUID uuid = UUID.fromString(res.getString(1));
                final OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
                ArchePersona[] prs = this.personas.get(uuid);
                if (prs == null) {
                    final long days = (time - p.getLastPlayed()) / 86400000L;
                    if (days > range) {
                        continue;
                    }
                    prs = new ArchePersona[4];
                    this.personas.put(uuid, prs);
                }
                final ArchePersona persona = this.buildPersona(res, p);
                prs[persona.getId()] = persona;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            for (final ArchePersona[] prs2 : this.getPersonas()) {
                boolean current = false;
                for (final ArchePersona p2 : prs2) {
                    if (p2 != null) {
                        if (p2.isCurrent()) {
                            if (!current) {
                                current = true;
                            }
                            else {
                                ArcheCore.getPlugin().getLogger().warning("Player " + p2.getPlayerName() + " has simultaneous current Personas. Fixing now...");
                                p2.setCurrent(false);
                            }
                        }
                    }
                }
                if (!current) {
                    for (final ArchePersona p2 : prs2) {
                        if (p2 != null) {
                            ArcheCore.getPlugin().getLogger().warning("Player " + p2.getPlayerName() + " was preloaded with no Persona set as current. Fixing now.");
                            p2.setCurrent(true);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    void deleteSkills(final ArchePersona p) {
        for (final String sname : ArcheSkillFactory.getSkills().keySet()) {
            this.buffer.put(new DataTask(4, "sk_" + sname, null, p.sqlCriteria));
        }
    }
    
    @Override
    public void ageUs() {
        final Map<String, Object> crits = Maps.newHashMap();
        crits.put("autoage", 1);
        final Map<String, Object> vals = Maps.newHashMap();
        vals.put("age", new Syntax("age+1"));
        final DataTask s = new DataTask(3, "persona", vals, crits);
        this.buffer.put(s);
        for (final ArchePersona[] prs : this.getPersonas()) {
            if (prs == null) {
                continue;
            }
            for (final ArchePersona p : prs) {
                if (p != null) {
                    if (p.doesAutoAge()) {
                        final ArchePersona archePersona = p;
                        ++archePersona.age;
                    }
                }
            }
        }
    }
    
    static {
        instance = new ArchePersonaHandler();
    }
}
