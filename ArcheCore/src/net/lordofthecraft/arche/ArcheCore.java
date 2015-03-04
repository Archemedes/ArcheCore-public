package net.lordofthecraft.arche;

import org.bukkit.plugin.java.*;
import net.lordofthecraft.arche.SQL.*;
import net.lordofthecraft.arche.save.tasks.*;
import org.bukkit.entity.*;
import com.google.common.collect.*;
import net.lordofthecraft.arche.save.*;
import java.util.*;
import java.sql.*;
import net.lordofthecraft.arche.skill.*;
import org.bukkit.command.*;
import net.lordofthecraft.arche.commands.*;
import org.bukkit.event.*;
import net.lordofthecraft.arche.listener.*;
import org.bukkit.plugin.*;
import org.bukkit.*;
import java.io.*;
import org.bukkit.configuration.file.*;
import org.bukkit.configuration.*;
import net.lordofthecraft.arche.persona.*;
import org.bukkit.block.*;
import net.lordofthecraft.arche.help.*;
import org.bukkit.inventory.*;
import net.lordofthecraft.arche.interfaces.*;

public class ArcheCore extends JavaPlugin implements IArcheCore
{
    private static ArcheCore instance;
    private SQLHandler sqlHandler;
    private SaveHandler saveHandler;
    private BlockRegistry blockRegistry;
    private ArchePersonaHandler personaHandler;
    private HelpDesk helpdesk;
    private ArcheTimer timer;
    private UsernameLogger usernameLogger;
    private Economy economy;
    private boolean helpOverriden;
    private boolean legacyCommands;
    private boolean showXpToPlayers;
    private boolean racialBonuses;
    private int nameChangeDelay;
    private int personaChangeDelay;
    private boolean enablePrefixes;
    private boolean modifyDisplayNames;
    private boolean debugMode;
    private int cachePersonas;
    private int newbieDelay;
    private int newbieProtectDelay;
    private boolean protectiveTeleport;
    private boolean teleportNewbies;
    private UUID newbieWorldUUID;
    private boolean useWiki;
    private Thread saverThread;
    
    public ArcheCore() {
        super();
        this.saverThread = null;
    }
    
    public void onDisable() {
        this.saveHandler.put(new EndOfStreamTask());
        for (final Player p : Bukkit.getOnlinePlayers()) {
            p.closeInventory();
            RaceBonusHandler.reset(p);
        }
        if (this.saverThread != null) {
            try {
                this.saverThread.join();
            }
            catch (InterruptedException e) {
                this.getLogger().severe("ArcheCore was interrupted prematurely while waiting for its saver thread to resolve.");
                e.printStackTrace();
            }
        }
        this.sqlHandler.close();
    }
    
    public void onLoad() {
        TreasureChest.init((Plugin)(ArcheCore.instance = this));
    }
    
    public void onEnable() {
        this.initConfig();
        this.sqlHandler = new SQLHandler((Plugin)this, "ArcheCore");
        this.saveHandler = SaveHandler.getInstance();
        this.blockRegistry = new BlockRegistry();
        this.personaHandler = ArchePersonaHandler.getInstance();
        this.helpdesk = HelpDesk.getInstance();
        this.timer = (this.debugMode ? new ArcheTimer((Plugin)this) : null);
        this.personaHandler.setModifyDisplayNames(this.modifyDisplayNames);
        this.usernameLogger = new UsernameLogger();
        LinkedHashMap<String, String> cols = Maps.newLinkedHashMap();
        cols.put("player", "TEXT");
        cols.put("id", "INT");
        cols.put("name", "TEXT");
        cols.put("age", "INT");
        cols.put("race", "TEXT");
        cols.put("rheader", "TEXT");
        cols.put("gender", "INT");
        cols.put("desc", "TEXT");
        cols.put("prefix", "TEXT");
        cols.put("current", "INT DEFAULT 1");
        cols.put("autoage", "INT DEFAULT 1");
        cols.put("stat_played", "INT DEFAULT 0");
        cols.put("stat_chars", "INT DEFAULT 0");
        cols.put("stat_renamed", "INT DEFAULT 0");
        cols.put("skill_xpgain", "INT DEFAULT 1");
        cols.put("skill_selected", "TEXT");
        cols.put("world", "TEXT");
        cols.put("x", "INT");
        cols.put("y", "INT");
        cols.put("z", "INT");
        cols.put("inv", "TEXT");
        cols.put("money", "REAL DEFAULT 0");
        cols.put("skill_primary", "TEXT");
        cols.put("skill_secondary", "TEXT");
        cols.put("skill_tertiary", "TEXT");
        cols.put("PRIMARY KEY (player, id)", "ON CONFLICT REPLACE");
        this.sqlHandler.createTable("persona", cols);
        cols = Maps.newLinkedHashMap();
        cols.put("player", "TEXT NOT NULL");
        cols.put("id", "INT NOT NULL");
        cols.put("name", "TEXT NOT NULL");
        cols.put("UNIQUE (player, id, name)", "ON CONFLICT IGNORE");
        this.sqlHandler.createTable("persona_names", cols);
        cols = Maps.newLinkedHashMap();
        cols.put("world", "TEXT NOT NULL");
        cols.put("x", "INT");
        cols.put("y", "INT");
        cols.put("z", "INT");
        cols.put("UNIQUE (world, x, y, z)", "ON CONFLICT IGNORE");
        this.sqlHandler.createTable("blockregistry", cols);
        this.sqlHandler.execute("DELETE FROM blockregistry WHERE ROWID IN (SELECT ROWID FROM blockregistry ORDER BY ROWID DESC LIMIT -1 OFFSET 5000)");
        try {
            final ResultSet res = this.sqlHandler.query("SELECT * FROM blockregistry");
            while (res.next()) {
                final String world = res.getString(1);
                final int x = res.getInt(2);
                final int y = res.getInt(3);
                final int z = res.getInt(4);
                final WeakBlock wb = new WeakBlock(world, x, y, z);
                this.blockRegistry.playerPlaced.add(wb);
            }
            res.getStatement().close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)this, (Runnable)new Runnable() {
            @Override
            public void run() {
                if (ArcheCore.this.willCachePersonas()) {
                    final long time = System.currentTimeMillis();
                    ArcheCore.this.getLogger().info("Preloading Personas as far back as " + ArcheCore.this.cachePersonas + " days");
                    ArcheCore.this.personaHandler.initPreload(ArcheCore.this.cachePersonas);
                    ArcheCore.this.getLogger().info("Personas were loaded in " + (System.currentTimeMillis() - time) + "ms.");
                }
                for (final Player p : Bukkit.getOnlinePlayers()) {
                    ArcheCore.this.personaHandler.initPlayer(p);
                }
                ArcheCore.this.saverThread = new Thread(new DataSaveRunnable(ArcheCore.this.saveHandler, ArcheCore.this.timer, ArcheCore.this.sqlHandler), "ArcheCore SQL Consumer");
                ArcheCore.this.saverThread.start();
            }
        });
        new TimeTrackerRunnable(this.personaHandler).runTaskTimer((Plugin)this, 2400L, 1200L);
        this.initHelp();
        this.initCommands();
        this.initListeners();
    }
    
    private void initConfig() {
        final FileConfiguration config = this.getConfig();
        this.saveDefaultConfig();
        this.helpOverriden = config.getBoolean("override.help.command");
        this.legacyCommands = config.getBoolean("enable.legacy.commands");
        this.nameChangeDelay = config.getInt("name.change.delay");
        this.personaChangeDelay = config.getInt("persona.change.delay");
        this.showXpToPlayers = config.getBoolean("show.exp.values");
        this.racialBonuses = config.getBoolean("enable.racial.bonuses");
        this.enablePrefixes = config.getBoolean("enable.persona.prefix");
        this.modifyDisplayNames = config.getBoolean("modify.player.displayname");
        this.newbieProtectDelay = config.getInt("persona.newbie.protect");
        this.debugMode = config.getBoolean("enable.debug.mode");
        this.newbieDelay = config.getInt("newbie.notification");
        this.useWiki = config.getBoolean("enable.wiki.lookup");
        this.cachePersonas = config.getInt("persona.cache.time");
        this.protectiveTeleport = config.getBoolean("teleport.to.rescue");
        this.teleportNewbies = config.getBoolean("new.persona.to.spawn");
        if (config.getBoolean("bonus.xp.racial")) {
            ArcheSkillFactory.activateXpMod(ExpModifier.RACIAL);
        }
        if (config.getBoolean("bonus.xp.playtime")) {
            ArcheSkillFactory.activateXpMod(ExpModifier.PLAYTIME);
        }
        if (config.getBoolean("bonus.xp.autoage")) {
            ArcheSkillFactory.activateXpMod(ExpModifier.AUTOAGE);
        }
        if (this.teleportNewbies) {
            final World w = Bukkit.getWorld(config.getString("preferred.spawn.world"));
            if (w != null) {
                this.newbieWorldUUID = w.getUID();
            }
            else {
                this.getLogger().info("Could not find config-specified world. Will use default world instead.");
            }
        }
        if (config.getBoolean("enable.economy")) {
            this.economy = new ArcheEconomy(config);
        }
    }
    
    private void initCommands() {
        this.getCommand("archehelp").setExecutor((CommandExecutor)new CommandArchehelp(this.helpdesk, this.helpOverriden));
        this.getCommand("helpmenu").setExecutor((CommandExecutor)new CommandHelpMenu(this.helpdesk));
        this.getCommand("persona").setExecutor((CommandExecutor)new CommandPersona(this.helpdesk, this.personaHandler, this.nameChangeDelay, this.enablePrefixes));
        this.getCommand("skill").setExecutor((CommandExecutor)new CommandSkill(this.helpdesk, this.showXpToPlayers));
        this.getCommand("beaconme").setExecutor((CommandExecutor)new CommandBeaconme());
        this.getCommand("treasurechest").setExecutor((CommandExecutor)new CommandTreasurechest());
        this.getCommand("realname").setExecutor((CommandExecutor)new CommandRealname(this));
        this.getCommand("autoage").setExecutor((CommandExecutor)new CommandAutoage(this));
        this.getCommand("money").setExecutor((CommandExecutor)new CommandMoney(this.helpdesk, this.economy));
        this.getCommand("namelog").setExecutor((CommandExecutor)new CommandNamelog());
        this.getCommand("arsql").setExecutor((CommandExecutor)new CommandSql());
    }
    
    private void initListeners() {
        final PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents((Listener)new PlayerJoinListener(this.personaHandler), (Plugin)this);
        pm.registerEvents((Listener)new PlayerInteractListener(this), (Plugin)this);
        pm.registerEvents((Listener)new BeaconMenuListener(this, this.personaChangeDelay), (Plugin)this);
        pm.registerEvents((Listener)new HelpMenuListener((Plugin)this, this.helpdesk), (Plugin)this);
        pm.registerEvents((Listener)new PlayerChatListener(), (Plugin)this);
        pm.registerEvents((Listener)new TreasureChestListener(), (Plugin)this);
        pm.registerEvents((Listener)new BlockRegistryListener(this.blockRegistry), (Plugin)this);
        if (this.helpOverriden) {
            pm.registerEvents((Listener)new HelpOverrideListener(), (Plugin)this);
        }
        if (this.legacyCommands) {
            pm.registerEvents((Listener)new LegacyCommandsListener(), (Plugin)this);
        }
        if (this.racialBonuses) {
            pm.registerEvents((Listener)new RacialBonusListener(this, this.getPersonaHandler()), (Plugin)this);
        }
        if (this.newbieProtectDelay > 0) {
            pm.registerEvents((Listener)new NewbieProtectListener(this.personaHandler, this.newbieProtectDelay), (Plugin)this);
        }
        if (this.usesEconomy() && this.economy.getFractionLostOnDeath() > 0.0) {
            pm.registerEvents((Listener)new EconomyListener(this.economy), (Plugin)this);
        }
    }
    
    private void initHelp() {
        final String div = ChatColor.LIGHT_PURPLE + "\n--------------------------------------------------\n";
        final String persHelp = ChatColor.YELLOW + "Your " + ChatColor.ITALIC + "Persona" + ChatColor.YELLOW + " is an uncouth sailor, fair Elven maiden or parent-slaying Orc. " + "in Lord of the Craft, you speak and act as your current Persona, and know only what they know." + div + ChatColor.GREEN + "Once accepted, creating your Persona is the first step of your adventure." + " You can later remove or create new Personas, by finding a " + ChatColor.ITALIC + "@Beacon@" + ChatColor.GREEN + " and right-clicking it.";
        final String commandHelp = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Your essential commands are as follows: " + div + ChatColor.GRAY + "" + ChatColor.ITALIC + (this.helpOverriden ? "$/help$" : "$/archehelp$") + ChatColor.GRAY + ": Provides a useful database of help topics.\n" + ChatColor.GOLD + "" + ChatColor.ITALIC + "$/helpmenu$" + ChatColor.GOLD + ": The same help topics, provided in a menu form.\n" + ChatColor.BLUE + "" + ChatColor.ITALIC + "$/persona$" + ChatColor.BLUE + ": See others' Personas and modify your own.\n" + ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "$/skill$" + ChatColor.LIGHT_PURPLE + ": See the Skills your Persona can learn";
        final String beaconInfo = "A " + ChatColor.ITALIC + "Beacon" + ChatColor.RESET + " lets you control most Persona-related tasks. Finding and right-clicking a beacon" + " grants access to, among other things, Persona modification and the plugin help files (which are also accessible via @commands@). Knowing where Beacons are located will be crucial for " + " orienting yourself with the ArcheCore plugin.";
        final String profession = ChatColor.GRAY + "/sk [skill] select {main/second/bonus}: " + ChatColor.GOLD + "Select a profession.\n" + ChatColor.GREEN + "You have to select a profession to raise that skill to the maximum possible tier. You can, however, experiment with any skill before committing to a profession.\n" + ChatColor.BLUE + "Depending on your Persona's race, you might also have a Racial Skill. Racial skills can be freelly leveled and do not need to be selected.\n" + ChatColor.GRAY + "Normally, you can be Adequate in any non-selected skill. If you pick a primary profession, this cap is tightened down to let you be Clumsy. If you also pick a secondary, your other skills will be locked at the Inept tier.";
        this.addHelp("Persona", persHelp, Material.REDSTONE_COMPARATOR);
        this.addHelp("Commands", commandHelp, Material.COMMAND);
        this.addHelp("Professions", profession, Material.BEDROCK);
        this.addInfo("Beacon", beaconInfo);
        if (!new File(this.getDataFolder(), "helpfiles.yml").exists()) {
            this.saveResource("helpfiles.yml", false);
        }
        FileConfiguration c = (FileConfiguration)YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "helpfiles.yml"));
        for (final String key : c.getKeys(false)) {
            if (c.isConfigurationSection(key)) {
                final ConfigurationSection section = c.getConfigurationSection(key);
                final String name = section.isString("topic") ? section.getString("topic") : key;
                String desc = null;
                if (!section.isString("content")) {
                    continue;
                }
                desc = section.getString("content").replace('&', '§');
                if (section.isString("icon")) {
                    try {
                        final Material m = Material.valueOf(section.getString("icon"));
                        this.addHelp(name, desc, m);
                    }
                    catch (IllegalArgumentException e) {
                        this.addHelp(name, desc);
                    }
                }
                else {
                    this.addHelp(name, desc);
                }
            }
        }
        if (!new File(this.getDataFolder(), "infofiles.yml").exists()) {
            this.saveResource("infofiles.yml", false);
        }
        c = (FileConfiguration)YamlConfiguration.loadConfiguration(new File(this.getDataFolder(), "infofiles.yml"));
        for (final String key : c.getKeys(false)) {
            if (c.isConfigurationSection(key)) {
                final ConfigurationSection section = c.getConfigurationSection(key);
                final String name = section.isString("topic") ? section.getString("topic") : key;
                String desc = null;
                if (!section.isString("content")) {
                    continue;
                }
                desc = section.getString("content").replace('&', '§');
                this.addInfo(name, desc);
            }
        }
    }
    
    public PersonaKey composePersonaKey(final UUID uuid, final int pid) {
        return getPersonaKey(uuid, pid);
    }
    
    public static PersonaKey getPersonaKey(final UUID uuid, final int pid) {
        return new ArchePersonaKey(uuid, pid);
    }
    
    public static Player getPlayer(final UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }
    
    public static ArcheCore getPlugin() {
        return ArcheCore.instance;
    }
    
    public static IArcheCore getControls() {
        return ArcheCore.instance;
    }
    
    public boolean willLogUsernames() {
        return true;
    }
    
    public UsernameLogger getUsernameLogger() {
        return this.usernameLogger;
    }
    
    public boolean debugMode() {
        return this.debugMode;
    }
    
    public ArcheTimer getMethodTimer() {
        return this.timer;
    }
    
    public BlockRegistry getBlockRegistry() {
        return this.blockRegistry;
    }
    
    public boolean isBlockPlayerPlaced(final Block b) {
        return this.blockRegistry.isPlayerPlaced(b);
    }
    
    public ArchePersonaHandler getPersonaHandler() {
        return this.personaHandler;
    }
    
    public SQLHandler getSQLHandler() {
        return this.sqlHandler;
    }
    
    public void addHelp(final HelpFile helpfile) {
        this.helpdesk.addTopic(helpfile);
    }
    
    public void addHelp(final String topic, final String output) {
        this.helpdesk.addTopic(topic, output);
    }
    
    public void addHelp(final String topic, final String output, final Material icon) {
        this.helpdesk.addTopic(topic, output, icon);
    }
    
    public void addInfo(final String topic, final String output) {
        this.helpdesk.addInfoTopic(topic, output);
    }
    
    public Skill getSkill(final String skillName) {
        return ArcheSkillFactory.getSkill(skillName);
    }
    
    public Skill createSkill(final String skillName) {
        try {
            return ArcheSkillFactory.createSkill(skillName);
        }
        catch (ArcheSkillFactory.DuplicateSkillException e) {
            this.getLogger().severe("Duplicate skill detected: " + skillName);
            return ArcheSkillFactory.getSkill(skillName);
        }
    }
    
    public SkillFactory registerNewSkill(final String skillName) {
        try {
            return ArcheSkillFactory.registerNewSkill(skillName);
        }
        catch (ArcheSkillFactory.DuplicateSkillException e) {
            this.getLogger().severe("Duplicate skill detected: " + skillName);
            return null;
        }
    }
    
    public ItemStack giveSkillTome(final Skill skill) {
        return SkillTome.giveTome(skill);
    }
    
    public ItemStack giveTreasureChest() {
        return TreasureChest.giveChest();
    }
    
    public boolean areRacialBonusesEnabled() {
        return this.racialBonuses;
    }
    
    public boolean arePrefixesEnabled() {
        return this.enablePrefixes;
    }
    
    public int getNewbieProtectDelay() {
        return this.newbieProtectDelay;
    }
    
    public int getNewbieNotificationDelay() {
        return this.newbieDelay;
    }
    
    public boolean getWikiUsage() {
        return this.useWiki;
    }
    
    public boolean willCachePersonas() {
        return this.cachePersonas > 0;
    }
    
    public boolean willModifyDisplayNames() {
        return this.personaHandler.willModifyDisplayNames();
    }
    
    public boolean usesEconomy() {
        return this.economy != null;
    }
    
    public Economy getEconomy() {
        return this.economy;
    }
    
    public boolean teleportNewPersonas() {
        return this.teleportNewbies;
    }
    
    public World getNewPersonaWorld() {
        if (this.newbieWorldUUID != null) {
            return Bukkit.getWorld(this.newbieWorldUUID);
        }
        return null;
    }
    
    public boolean teleportProtectively() {
        return this.protectiveTeleport;
    }
}
