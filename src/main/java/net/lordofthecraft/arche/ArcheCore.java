package net.lordofthecraft.arche;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.commands.*;
import net.lordofthecraft.arche.commands.tab.CommandAttributeTabCompleter;
import net.lordofthecraft.arche.commands.tab.CommandHelpTabCompleter;
import net.lordofthecraft.arche.commands.tab.CommandPersonaTabCompleter;
import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.help.HelpFile;
import net.lordofthecraft.arche.interfaces.*;
import net.lordofthecraft.arche.listener.*;
import net.lordofthecraft.arche.persona.ArcheEconomy;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.FatigueDecreaser;
import net.lordofthecraft.arche.save.Consumer;
import net.lordofthecraft.arche.save.DumpedDBReader;
import net.lordofthecraft.arche.save.rows.player.ReplacePlayerRow;
import net.lordofthecraft.arche.seasons.LotcianCalendar;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import net.lordofthecraft.arche.skill.ArcheSkillFactory.DuplicateSkillException;
import net.lordofthecraft.arche.skin.SkinCache;
import net.lordofthecraft.arche.util.CaseString;
import net.lordofthecraft.arche.util.SQLUtil;
import net.lordofthecraft.arche.util.WeakBlock;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.UUID;
import java.util.logging.Level;

public class ArcheCore extends JavaPlugin implements IArcheCore {
    private static ArcheCore instance;

    private SQLHandler sqlHandler;
    private BlockRegistry blockRegistry;
    private ArchePersonaHandler personaHandler;
    private ArcheFatigueHandler fatigueHandler;
    private HelpDesk helpdesk;
    private SkinCache skinCache;
    private ArcheTimer timer;
    private ArcheEconomy economy;
    private LotcianCalendar calendar;
    private Consumer archeConsumer;
    private Timer archeTimer = null;

    //Config settings
    private int maxPersonaSlots;
    private boolean helpOverriden;
    private boolean legacyCommands;
    private boolean showXpToPlayers; //currently unused. Reintroduce when tiers do
    private boolean racialBonuses;
    private boolean damageBonuses;
    private int nameChangeDelay;
    private int personaChangeDelay;
    private int personaPermakillDelay;
    private boolean enablePrefixes;
    private boolean modifyDisplayNames;
    private boolean debugMode;
    private int newbieDelay;
    private int newbieProtectDelay;
    private boolean protectiveTeleport;
    private boolean teleportNewbies;
    private UUID newbieWorldUUID;
    private boolean useWiki;
    private String worldName;
    private boolean enderchestInMenu;
    private boolean usingMySQL;
    private int fullFatigueRestore;

    //Bungee
    private boolean canCreatePersonas;

    //Consumer variables
    private int consumerRun;
    private int consumerForceProcessMin;
    private int consumerRunDelay;
    private boolean consumerShouldUseBukkitScheduler;
    private int consumerWarningSize;
    private boolean debugConsumer;

    private boolean useDeleteProcedure = false;

    private int blockregistryPurgeDelay;
    private boolean blockregistryKillCustom;
    private boolean shouldClone = false;
    
    private final BiMap<UUID, CaseString> playerNameMap = HashBiMap.create();
    
    public static PersonaKey getPersonaKey(UUID uuid, int pid) {
        Persona pers = getPersonaControls().getPersona(uuid, pid);
        if (pers != null) {
            return pers.getPersonaKey();
        } else {
            return null;
        }
    }

    public static Player getPlayer(UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    public static ArcheCore getPlugin() {
        return instance;
    }

    public static PersonaHandler getPersonaControls() {
        return getControls().getPersonaHandler();
    }

    public static Economy getEconomyControls() {
        return getControls().getEconomy();
    }

    public static SQLHandler getSQLControls() {
        return getControls().getSQLHandler();
    }

    public static IConsumer getConsumerControls() {
        return getControls().getConsumer();
    }

    public static OfflinePersona getPersona(int persona_id) {
        return instance.personaHandler.getPersonaById(persona_id);
    }

    public static Persona getPersona(Player p) {
        return instance.getPersonaHandler().getPersona(p);
    }

    public static boolean hasPersona(Player p) {
        return instance.getPersonaHandler().hasPersona(p);
    }

    public static boolean usingSQLite() {
        return instance.isUsingSQLite();
    }

    /**
     * Fetch the current instance of the ArcheCore plugin
     *
     * @return The ArcheCore singleton
     */
    public static IArcheCore getControls() {
        return instance;
    }

    public void onDisable() {

        Bukkit.getOnlinePlayers().forEach(p -> {
            //This part must be done for safety reasons.
            //Disables are messy, and in the brief period of Bukkit downtime
            //Players may shift inventories around and dupe items they shouldn't dupe
            p.closeInventory();

            personaHandler.leavePlayer(p);
        });

        if (archeTimer != null) {
            archeTimer.cancel();
        }
        getServer().getScheduler().cancelTasks(this);
        if (archeConsumer != null) {
            getLogger().info("[Consumer] Proceeding to shutdown SQL Consumer...");
            archeConsumer.bypassForce();
            archeConsumer.run();
            if (archeConsumer.getQueueSize() > 0) {
                int tries = 9;
                while (archeConsumer.getQueueSize() > 0) {
                    getLogger().info("[Consumer] Remaining queue size: " + archeConsumer.getQueueSize());
                    if (tries > 0) {
                        getLogger().info("[Consumer] Remaining Tries: " + tries);
                    } else {
                        getLogger().warning("[Consumer] We failed to save the ArcheCore Database!!! This is potentially REALLY BAD!!! We're going to try to write all pending changes to a file!");
                        try {
                            archeConsumer.writeToFile();
                            getLogger().info("[Consumer] We've successfully dumped all pending DB changes into files, we will attempt to parse them on next start up.");
                        } catch (final FileNotFoundException ex) {
                            getLogger().severe("[Consumer] Well, this is ACTUALLY bad. We've failed to save the Database and failed to write to the database changes to a file. All pending changes have been lost irrevocably. Consider manual refunds, suicide, or both. FeelsAmazingMan :gun:");
                            break;
                        }
                    }
                    archeConsumer.run();
                    tries--;
                }
            }

        }
        if (sqlHandler != null) {
            sqlHandler.close();
            if (shouldClone && sqlHandler instanceof ArcheSQLiteHandler) {
                ((ArcheSQLiteHandler) sqlHandler).cloneDB();
            }
        }
    }

    @Override
    public void onLoad(){
        instance = this;
    }

    @Override
    public void onEnable() {
    	
    	
    	long timeOnEnable = System.currentTimeMillis();

        //Initialize our config file
        initConfig();
        
        //Set up the debug timer, if necessary.
    	timer = debugMode? new ArcheTimer(this) : null;

        //Setup the SQL stuff for ArcheCore
        initSql();
        

        //Find our Singletons and assign them.
        blockRegistry = new BlockRegistry();
        personaHandler = ArchePersonaHandler.getInstance();
        fatigueHandler = ArcheFatigueHandler.getInstance();
        helpdesk = HelpDesk.getInstance();
        skinCache = SkinCache.getInstance();
        
        ResultSet res = null;
        try(Connection c = sqlHandler.getConnection()){
            if (timer != null) timer.startTiming("Loading player UUID/name map");
        	res = c.createStatement().executeQuery("SELECT player,player_name FROM players");
        	while(res.next()) {
        		UUID uuid = UUID.fromString(res.getString("player"));
        		String name = res.getString("player_name");
        		playerNameMap.put(uuid, new CaseString(name));
        	}
        	res.close();
            res.getStatement().close();
            if (timer != null) timer.stopTiming("Loading player UUID/name map");
            
            res = c.createStatement().executeQuery("SELECT world,x,y,z,data FROM blockregistry");
            while(res.next()){
                String world = res.getString("world");
                int x = res.getInt("x");
                int y = res.getInt("y");
                int z = res.getInt("z");
                String data = res.getString("data");
                WeakBlock wb = new WeakBlock(world, x, y, z);
                blockRegistry.playerPlaced.add(wb);
                if (data != null) {
                    blockRegistry.data.putIfAbsent(wb, data);
                }
            }
        }
        catch(SQLException e){e.printStackTrace();}
        finally {SQLUtil.closeStatement(res);}

        personaHandler.onEnable();
        economy.onEnable();
        
        fatigueHandler.fatigueDecreaseHours = this.fullFatigueRestore;
        personaHandler.setModifyDisplayNames(modifyDisplayNames);
        
        //Preloads our skills from SQL so that they are persistent at all times.
        //May also create a command/field to flag a skill as forcibly disabled.
        ArcheSkillFactory.preloadSkills(sqlHandler);

        

        
        //Incase of a reload, load all Personas for currently logged in players
        for (Player p : Bukkit.getOnlinePlayers()) {
            personaHandler.getPersonaStore().loadPersonas(p.getName(), p.getUniqueId());
            personaHandler.joinPlayer(p);
        }

        //Start tracking Personas and their playtime.
        new TimeTrackerRunnable(personaHandler).runTaskTimerAsynchronously(this, 2203, 1200);

        //Start gradually reducing the fatigue of Personas in 20-minute intervals
        if(fullFatigueRestore > 0) new FatigueDecreaser(fullFatigueRestore).runTaskTimer(this, 173, 20*60*20);

        //Initialize some essential help topics
        initHelp();

        //Redirect Plugin commands
        initCommands();

        //Start all our Event listeners
        initListeners();
        
        getLogger().info("ArcheCore booted up in " + (System.currentTimeMillis() - timeOnEnable) + "ms.");
    }

    private void initConfig(){
        FileConfiguration config = getConfig(); // Get the config file either out of our .jar our datafolder
        saveDefaultConfig(); //Save the config file to disk if it doesn't exist yet.

        maxPersonaSlots = Math.max(1, Math.min(17, config.getInt("persona.slots.maximum")));
        helpOverriden = config.getBoolean("override.help.command");
        legacyCommands = config.getBoolean("enable.legacy.commands");
        nameChangeDelay = config.getInt("name.change.delay");
        personaChangeDelay = config.getInt("persona.change.delay");
        personaPermakillDelay = config.getInt("persona.permakill.delay");
        showXpToPlayers = config.getBoolean("show.exp.values");
        racialBonuses = config.getBoolean("enable.racial.bonuses");
        damageBonuses = config.getBoolean("enable.racial.damage.bonuses");
        enablePrefixes = config.getBoolean("enable.persona.prefix");
        modifyDisplayNames = config.getBoolean("modify.player.displayname");
        newbieProtectDelay = config.getInt("persona.newbie.protect");
        debugMode = config.getBoolean("enable.debug.mode");
        newbieDelay = config.getInt("newbie.notification");
        useWiki = config.getBoolean("enable.wiki.lookup");
        protectiveTeleport = config.getBoolean("teleport.to.rescue");
        teleportNewbies = config.getBoolean("new.persona.to.spawn");
        worldName = config.getString("server.world.name");
        enderchestInMenu = config.getBoolean("persona.menu.enderchest");
        usingMySQL = config.getBoolean("enable.mysql");
        fullFatigueRestore = config.getInt("persona.fatigue.restore");
        canCreatePersonas = config.getBoolean("can.create.personas");

        //Consumer variables
        consumerRun = config.getInt("consumer.run.time");
        consumerForceProcessMin = config.getInt("consumer.force.process.minimum");
        consumerRunDelay = config.getInt("consumer.run.delay");
        consumerShouldUseBukkitScheduler = config.getBoolean("consumer.use.bukkit.scheduler");
        consumerWarningSize = config.getInt("consumer.queue.warning.size");
        debugConsumer = config.getBoolean("consumer.debug");

        if (debugMode) {
            getLogger().info("[Consumer] Started with the following config options: " +
                    "runTime: " + consumerRun + ", " +
                    "forceProcessMin: " + consumerForceProcessMin + ", " +
                    "runDelay: " + consumerRun + ", " +
                    "useBukkitScheduler?: " + consumerShouldUseBukkitScheduler + ", " +
                    "warningSize: " + consumerWarningSize);
        }

        if(teleportNewbies){
            World w = Bukkit.getWorld(config.getString("preferred.spawn.world"));
            if(w != null) this.newbieWorldUUID = w.getUID();
            else getLogger().info("Could not find config-specified world. Will use default world instead.");
        }

        if(config.getBoolean("enable.economy"))
            economy = new ArcheEconomy(config);
        
		List<String> trackedWorlds = config.getStringList("seasonal.tracked.worlds");
		int offsetYears = config.getInt("calendar.offset.years");
		boolean switchBiomes = config.getBoolean("seasonal.biome.switch");
        calendar = new LotcianCalendar(trackedWorlds, switchBiomes, offsetYears);
        calendar.onEnable();
    }
    
    private void initSql() {
    	if(timer != null) timer.startTiming("ArcheCore SQL Init");
    	
        getLogger().info("Loading " + (usingMySQL ? "MySQL" : "SQLite") + " handler now.");
        int timeout = getConfig().getInt("hikari.timeoutms");
        if (usingMySQL) {
            String username = getConfig().getString("mysql.user");
            String password = getConfig().getString("mysql.password");
            try {
                getLogger().info("Logging into MySQL at " + WhySQLHandler.getUrl() + ", Username: " + username);
                sqlHandler = new WhySQLHandler(username, password, timeout);
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to initialize MySQL DB on url " + WhySQLHandler.getUrl() + " with username " + username + " and password " + password, e);
                usingMySQL = false;
                sqlHandler = new ArcheSQLiteHandler(this, "ArcheCore", timeout);
            }
        } else {
            sqlHandler = new ArcheSQLiteHandler(this, "ArcheCore", timeout);
        }

        //This will import data that might have failed to save before ArcheCore was last disabled.
        //MUST complete before we progress so it's done on the main thread, if a persona creation or deletion is in here that is VITAL that it is performed before we load anything.
        new DumpedDBReader(this).run();
        archeConsumer = new Consumer(sqlHandler, this, consumerRun, consumerForceProcessMin, consumerWarningSize, debugConsumer);
        
        if (consumerShouldUseBukkitScheduler) {
        	Bukkit.getScheduler().runTaskTimerAsynchronously(this, archeConsumer, consumerRunDelay < 20 ? 20 : consumerRunDelay, consumerRunDelay).getTaskId();
        	getLogger().info("[Consumer] Started using Bukkit Scheduler with a delay of " + consumerRunDelay + " ticks");
        } else {
            archeTimer = new Timer();
            archeTimer.schedule(archeConsumer, consumerRunDelay < 20 ? 1000 : consumerRunDelay * 50, consumerRunDelay * 50);
            getLogger().info("[Consumer] Started using Java Timer with a delay of " + (consumerRunDelay * 50) + "ms");
        }
        
        if (!ArcheTables.setUpSQLTables(sqlHandler)) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (sqlHandler instanceof WhySQLHandler) {
            sqlHandler.execute("DELETE FROM blockregistry WHERE date>DATE_ADD(NOW(), INTERVAL " + blockregistryPurgeDelay + " DAY)" + (blockregistryKillCustom ? " WHERE data IS NULL;" : ";"));
        } else {
            sqlHandler.execute("DELETE FROM blockregistry WHERE date>DateTime('Now', 'LocalTime', '+" + blockregistryPurgeDelay + " Day')" + (blockregistryKillCustom ? " WHERE data IS NULL;" : ";"));
        }
        
        if(timer != null) timer.stopTiming("ArcheCore SQL Init");
    }

    private void initCommands(){
        getCommand("archehelp").setExecutor(new CommandArchehelp(helpdesk, helpOverriden));
        getCommand("archehelp").setTabCompleter(new CommandHelpTabCompleter(helpdesk));
        getCommand("helpmenu").setExecutor(new CommandHelpMenu(helpdesk));
        getCommand("persona").setExecutor(new CommandPersona(helpdesk, personaHandler, nameChangeDelay, enablePrefixes));
        getCommand("persona").setTabCompleter(new CommandPersonaTabCompleter());
        getCommand("beaconme").setExecutor(new CommandBeaconme());
        getCommand("realname").setExecutor(new CommandRealname(this));
        getCommand("money").setExecutor(new CommandMoney(helpdesk, economy));
        getCommand("namelog").setExecutor(new CommandNamelog());
        getCommand("arsql").setExecutor(new CommandSql());
        getCommand("arclone").setExecutor(new CommandSqlClone());
        getCommand("newbies").setExecutor(new CommandNewbies(personaHandler));
        getCommand("racespawn").setExecutor(new CommandRaceSpawn(personaHandler));
        getCommand("attribute").setExecutor(new CommandAttribute());
        getCommand("attribute").setTabCompleter(new CommandAttributeTabCompleter());
        getCommand("skin").setExecutor(new CommandSkin(this));
        getCommand("date").setExecutor(new CommandDate(this));
    }

    private void initListeners(){
        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new PlayerJoinListener(personaHandler), this);
        pm.registerEvents(new PlayerInteractListener(this), this);
        pm.registerEvents(new BeaconMenuListener(this, personaChangeDelay), this);
        pm.registerEvents(new HelpMenuListener(this, helpdesk), this);
        pm.registerEvents(new PlayerChatListener(), this);
        pm.registerEvents(new BlockRegistryListener(blockRegistry), this);
        pm.registerEvents(new PersonaInventoryListener(), this);
        pm.registerEvents(new ArmorListener(), this);
        pm.registerEvents(new ExhaustionListener(), this);
        pm.registerEvents(new SeasonListener(calendar), this);

        if (showXpToPlayers) {
            pm.registerEvents(new ExperienceOrbListener(), this);
        }

        if(helpOverriden)
            pm.registerEvents(new HelpOverrideListener(), this);

        if(legacyCommands)
            pm.registerEvents(new LegacyCommandsListener(), this);

        if(racialBonuses){
            pm.registerEvents(new RacialBonusListener(this,getPersonaHandler()), this);
            pm.registerEvents(new ArmorPreventionListener(), this);
        }

        if(newbieProtectDelay > 0)
            pm.registerEvents(new NewbieProtectListener(personaHandler, newbieProtectDelay), this);

        if(usesEconomy() && economy.getFractionLostOnDeath() > 0.0d)
            pm.registerEvents(new EconomyListener(economy), this);
    }

    private void initHelp(){
        final String div = ChatColor.LIGHT_PURPLE +  "\n--------------------------------------------------\n";

        //Set the essential Help files
        String persHelp = ChatColor.YELLOW + "Your " + ChatColor.ITALIC + "Persona" + ChatColor.YELLOW + " is an uncouth sailor, fair Elven maiden or parent-slaying Orc. " +
                "in Lord of the Craft, you speak and act as your current Persona, and know only what they know." +
                div + ChatColor.GREEN + "Once accepted, creating your Persona is the first step of your adventure."
                + " You can later remove or create new Personas by using the $/me$ " + ChatColor.GREEN + " command.";

        String commandHelp = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Your essential commands are as follows: "
                + div + ChatColor.GRAY + "" + ChatColor.ITALIC +  (helpOverriden? "$/help$":"$/archehelp$") + ChatColor.GRAY + ": Provides a useful database of help topics.\n"
                + ChatColor.GOLD + "" + ChatColor.ITALIC + "$/helpmenu$" + ChatColor.GOLD + ": The same help topics, provided in a menu form.\n"
                + ChatColor.BLUE + "" + ChatColor.ITALIC + "$/persona$" + ChatColor.BLUE + ": See others' Personas and modify your own.\n"
                + ChatColor.DARK_GREEN+ "" + ChatColor.ITALIC + "$/me$"+ChatColor.DARK_GREEN+": Opens the Persona selection menu";

        String profession = ChatColor.GRAY + "/persona profession [skill]: " + ChatColor.GOLD + "Select a profession.\n"
                + ChatColor.GREEN + "You can select only a single profession and only ONCE, so choose very carefully.\n"
                + ChatColor.BLUE  + "Depending on your Persona's race, some professions may come easier to you than others.\n"
                + ChatColor.GRAY + "You receive special perks for your chosen profession, but beware, as these perks cause @@Fatigue@@.";

        String attributes = ChatColor.GRAY + "Attributes are the various base stats of your Persona. These stats can be anything from how fast your Persona runs to how much fatigue they accumulate. \n"
        		+ ChatColor.GREEN + "Various modifiers on items, spells and foodstuffs can affect these attributes for better or worse.\n"
                + ChatColor.LIGHT_PURPLE  + "Current persona modifiers are shown on an icon in the $/me$ " + ChatColor.LIGHT_PURPLE + "menu.";

        
        String fatigue = ChatColor.GRAY + "Fatigue accumulates when practicing your chosen @@<Professions>profession@@.\n"
                + ChatColor.GREEN + "You can still do everything while fatigued, but will not have access to your profession-related perks.\n"
                + ChatColor.LIGHT_PURPLE  + "your fatigue will reset over time, so it's best to simply take a break and wait it out. "
                + "If you are inpatient, having a drink at a tavern will also reduce a Persona's fatigue.\n";
        
        addHelp("Persona", persHelp, Material.REDSTONE_COMPARATOR, "archecore.mayuse");
        addHelp("Commands", commandHelp, Material.COMMAND);
        addHelp("Professions", profession, Material.BEDROCK);
        addHelp("Attributes", attributes, Material.GOLDEN_APPLE);   
        addHelp("Fatigue", fatigue, Material.BED);        
        
        //Create config-set Help Files
        if(!(new File(getDataFolder(), "helpfiles.yml").exists()))
            saveResource("helpfiles.yml", false);
        FileConfiguration c = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "helpfiles.yml"));
        for(String key : c.getKeys(false)){
            if(c.isConfigurationSection(key)){
                ConfigurationSection section = c.getConfigurationSection(key);
                String name = section.isString("topic")? section.getString("topic") : key;
                String permission = section.isString("permission") ? section.getString("permission") : "archecore.mayuse";

                String desc = null;
                if (section.isString("content")) desc = section.getString("content").replace('&', ChatColor.COLOR_CHAR);
                else continue;

                if(section.isString("icon")){
                    try{
                        Material m = Material.valueOf(section.getString("icon"));
                        addHelp(name, desc, m, permission);
                    }catch(IllegalArgumentException e){
                        addHelp(name, desc, permission);
                    }
                } else addHelp(name, desc, permission);
            }
        }

        //Create config-set info Files
        if(!(new File(getDataFolder(), "infofiles.yml").exists()))
            saveResource("infofiles.yml", false);
        c = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "infofiles.yml"));
        for(String key : c.getKeys(false)){
            if(c.isConfigurationSection(key)){
                ConfigurationSection section = c.getConfigurationSection(key);
                String name = section.isString("topic")? section.getString("topic") : key;
                String permission = section.isString("permission") ? section.getString("permission") : "archecore.mayuse";

                String desc = null;
                if (section.isString("content")) desc = section.getString("content").replace('&', ChatColor.COLOR_CHAR);
                else continue;

                addInfo(name, desc, permission);
            }
        }

    }

    @Override
    public PersonaKey composePersonaKey(UUID uuid, int pid){
        return getPersonaKey(uuid, pid);
    }

    public boolean debugMode(){
        return debugMode;
    }

    public static boolean isDebugging() {
        return instance.debugMode;
    }

    public ArcheTimer getMethodTimer(){
        return timer;
    }

    void setUseDeleteProcedure(boolean toset) {
        this.useDeleteProcedure = toset;
    }

    @Override
    public boolean logsPersonaDeletions() {
        return useDeleteProcedure;
    }

    @Override
    public BlockRegistry getBlockRegistry() {
        return blockRegistry;
    }

    @Override
    public boolean isBlockPlayerPlaced(Block b){
        return blockRegistry.isPlayerPlaced(b);
    }

    @Override
    public ArchePersonaHandler getPersonaHandler(){
        return personaHandler;
    }

    @Override
    public ArcheFatigueHandler getFatigueHandler() {
        return fatigueHandler;
    }

    @Override
    public SQLHandler getSQLHandler(){
        return sqlHandler;
    }

    @Override
    public SkinCache getSkinCache(){
        return skinCache;
    }

    @Override
    public IConsumer getConsumer() {
        return archeConsumer;
    }

    @Override
    public void addHelp(HelpFile helpfile){
        helpdesk.addTopic(helpfile);
    }

    @Override
    public void addHelp(String topic, String output, String permission) {
        helpdesk.addTopic(topic, output, permission);
    }

    @Override
    public void addHelp(String topic, String output){
        helpdesk.addTopic(topic, output);
    }

    @Override
    public void addHelp(String topic, String output, Material icon, String permission) {
        helpdesk.addTopic(topic, output, icon, permission);
    }

    @Override
    public void addHelp(String topic, String output, Material icon){
        helpdesk.addTopic(topic, output, icon);
    }

    @Override
    public void addInfo(String topic, String output){
        helpdesk.addInfoTopic(topic, output);
    }

    @Override
    public void addInfo(String topic, String output, String permission) {
        helpdesk.addInfoTopic(topic, output, permission);
    }

    @Override
    public Skill getSkill(String skillName){
        return ArcheSkillFactory.getSkill(skillName);
    }

    //These should just throw an exception in the method signature to allow for skills to handle them appropriately.
    //Especially with some being loaded from SQL this is important.

    @Override
    public Skill createSkill(String skillName, Plugin controller) throws DuplicateSkillException {
        return ArcheSkillFactory.createSkill(skillName, controller);
    }

    @Override
    public SkillFactory registerNewSkill(String skillName, Plugin controller) throws DuplicateSkillException {
        return ArcheSkillFactory.registerNewSkill(skillName, controller);
    }

    @Override
    public void setShouldClone(boolean val) { this.shouldClone = val; }

    @Override
    public boolean isCloning() { return shouldClone; }

    @Override
    public boolean areRacialBonusesEnabled(){
        return racialBonuses;
    }

    @Override
    public boolean areRacialDamageBonusesEnabled(){
        return damageBonuses;
    }

    @Override
    public boolean arePrefixesEnabled(){
        return enablePrefixes;
    }

    @Override
    public int getNewbieProtectDelay(){
        return newbieProtectDelay;
    }

    public int getNewbieNotificationDelay(){
        return newbieDelay;
    }

    public boolean getWikiUsage(){
        return useWiki;
    }
    
    public void updateNameMap(Player player) {
    	UUID u = player.getUniqueId();
    	String n = player.getName();
    	
    	if(!playerNameMap.containsKey(u) || !n.equals(playerNameMap.get(u).getValue())) {
    		if(isDebugging()) getLogger().info("[Debug] Updating Player Name Map: " + u + "=" + n);
    		playerNameMap.put(player.getUniqueId(), new CaseString(player.getName()));
        	getConsumer().queueRow(new ReplacePlayerRow(player));
    	}
    }
    
    @Override
    public String getPlayerNameFromUUID(UUID playerUUID) {
    	CaseString w = playerNameMap.get(playerUUID);
    	return w==null? null : w.getValue();
    }
    
    @Override
    public UUID getPlayerUUIDFromName(String playerName) {
    	CaseString w = new CaseString(playerName);
    	return playerNameMap.inverse().get(w);
    }

    @Override
    public boolean willModifyDisplayNames(){
        return personaHandler.willModifyDisplayNames();
    }

    @Override
    public boolean usesEconomy() {
        return economy != null;
    }
    
    @Override
    public LotcianCalendar getCalendar() {
    	return calendar;
    }

    @Override
    public Economy getEconomy() {
        return economy;
    }

    @Override
    public boolean teleportNewPersonas() {
        return teleportNewbies;
    }

    @Override
    public World getNewPersonaWorld() {
        if(newbieWorldUUID != null) return Bukkit.getWorld(newbieWorldUUID);
        else return null;
    }

    public boolean teleportProtectively(){
        return protectiveTeleport;
    }

    public ArcheCore getInstance(){
        return instance;
    }

    @Override
    public int getNewbieDelay() {
        return newbieProtectDelay;
    }

    @Override
    public String getServerWorldName() {
        return worldName;
    }

    @Override
    public boolean showEnderchestInMenu() {
        return enderchestInMenu;
    }

    @Override
    public boolean canCreatePersonas() {
        return canCreatePersonas;
    }

    @Override
    public int getNewPersonaPermakillDelay() {
        return personaPermakillDelay;
    }

    @Override
    public int personaSlots() {
        return this.maxPersonaSlots;
    }

    @Override
    public boolean isUsingSQLite() {
        return sqlHandler instanceof ArcheSQLiteHandler;
    }
}