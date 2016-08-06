package net.lordofthecraft.arche;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.commands.*;
import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.help.HelpFile;
import net.lordofthecraft.arche.interfaces.*;
import net.lordofthecraft.arche.listener.*;
import net.lordofthecraft.arche.persona.ArcheEconomy;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.ArchePersonaKey;
import net.lordofthecraft.arche.persona.RaceBonusHandler;
import net.lordofthecraft.arche.save.DataSaveRunnable;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.EndOfStreamTask;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import net.lordofthecraft.arche.skill.ArcheSkillFactory.DuplicateSkillException;
import net.lordofthecraft.arche.skill.ExpModifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ArcheCore extends JavaPlugin implements IArcheCore {
	private static ArcheCore instance;

	private SQLHandler sqlHandler;
	private SaveHandler saveHandler;
	private BlockRegistry blockRegistry;
	private ArchePersonaHandler personaHandler;
	private HelpDesk helpdesk;
	private ArcheTimer timer;
	private Economy economy;
	private JistumaCollection jcoll;

	//Config settings
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
	private String worldName;
	private boolean racialSwingTimer;
	private boolean usingMySQL;

	private Thread saverThread = null;

	private boolean shouldClone = false;

	public static PersonaKey getPersonaKey(UUID uuid, int pid) {
		return new ArchePersonaKey(uuid, pid);
	}

	public static Player getPlayer(UUID uuid) {
		return Bukkit.getPlayer(uuid);
	}

	public static ArcheCore getPlugin() {
		return instance;
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

		saveHandler.put(new EndOfStreamTask());

		Bukkit.getOnlinePlayers().stream().forEach(p -> {
			//This part must be done for safety reasons.
			//Disables are messy, and in the brief period of Bukkit downtime
			//Players may shift inventories around and dupe items they shouldn't dupe
			p.closeInventory();

			//Attribute Bonuses stick around forever. To prevent lingering ones, just in
			//case the plugin is to be removed, we perform this method.
			RaceBonusHandler.reset(p);
		});

		if (saverThread != null) {
			try {
				saverThread.join();}
			catch (InterruptedException e) {
				getLogger().severe("ArcheCore was interrupted prematurely while waiting for its saver thread to resolve.");
				e.printStackTrace();
			}
		}
		sqlHandler.close();
		if (shouldClone && sqlHandler instanceof ArcheSQLiteHandler) {
			((ArcheSQLiteHandler) sqlHandler).cloneDB();
		}
	}

	@Override
	public void onLoad(){
		instance = this;

		TreasureChest.init(this);
	}

	@Override
	public void onEnable() {

		//Initialize our config file
		initConfig();

		//Find our Singletons and assign them.
		getLogger().info("Loading " + (usingMySQL ? "MySQL" : "SQLite") + " handler now.");
		if (usingMySQL) {
			String username = getConfig().getString("mysql.user");
			String password = getConfig().getString("mysql.password");
			try {
				getLogger().info("Logging into MySQL at " + WhySQLHandler.getUrl() + ", Username: " + username);
				sqlHandler = new WhySQLHandler(username, password);
			} catch (Exception e) {
				getLogger().log(Level.SEVERE, "Failed to initialize MySQL DB on url " + WhySQLHandler.getUrl() + " with username " + username + " and password " + password, e);
				sqlHandler = new ArcheSQLiteHandler(this, "ArcheCore");
			}
		} else {
			sqlHandler = new ArcheSQLiteHandler(this, "ArcheCore");
		}
		saveHandler = SaveHandler.getInstance();
		blockRegistry = new BlockRegistry();
		personaHandler = ArchePersonaHandler.getInstance();
		helpdesk = HelpDesk.getInstance();
		jcoll = new JistumaCollection(personaHandler);

		timer = debugMode? new ArcheTimer(this) : null;
		personaHandler.setModifyDisplayNames(modifyDisplayNames);

		//Create the Persona table
		Map<String,String> cols = Maps.newLinkedHashMap();
		cols.put("player", "TEXT"); //1
		cols.put("id", "INT");
		cols.put("name", "TEXT");
		cols.put("age", "INT");
		cols.put("race", "TEXT"); //5
		cols.put("rheader", "TEXT");
		cols.put("gender", "INT");
		cols.put("desc", "TEXT");
		cols.put("prefix", "TEXT");
		cols.put("current", "INT DEFAULT 1"); //10
		cols.put("autoage", "INT DEFAULT 1");
		cols.put("stat_played", "INT DEFAULT 0");
		cols.put("stat_chars", "INT DEFAULT 0");
		cols.put("stat_renamed", "INT DEFAULT 0");
		cols.put("skill_xpgain", "INT DEFAULT 1"); //15
		cols.put("skill_selected", "TEXT");
		cols.put("world", "TEXT");
		cols.put("x", "INT");
		cols.put("y", "INT");
		cols.put("z", "INT"); //20
		//cols.put("health", "REAL");
		//cols.put("hunger", "REAL");
		//cols.put("saturation", "REAL");
		cols.put("inv", "TEXT");
		cols.put("money", "REAL DEFAULT 0");
		cols.put("skill_primary", "TEXT");
		cols.put("skill_secondary", "TEXT");
		cols.put("skill_tertiary", "TEXT");
		cols.put("stat_creation", "LONG");
		cols.put("stat_playtime_past","INT");
		cols.put("PRIMARY KEY (player, id)", "ON CONFLICT REPLACE");
		sqlHandler.createTable("persona", cols);

		/*		cols = Maps.newLinkedHashMap();
		cols.put("player", "TEXT NOT NULL");
		cols.put("id", "INT NOT NULL");
		cols.put("wastaught", "INT");
		cols.put("hastaught", "INT");
		cols.put("boosted", "INT");
		cols.put("xplimit", "INT");
		cols.put("UNIQUE (player, id)", "ON CONFLICT IGNORE");
		cols.put("FOREIGN KEY (player, id)", "REFERENCES persona(player, id) ON DELETE CASCADE");
		sqlHandler.createTable("persona_ext", cols);*/

		cols = Maps.newLinkedHashMap();
		cols.put("player", "TEXT NOT NULL");
		cols.put("id", "INT NOT NULL");
		cols.put("name", "TEXT NOT NULL");
		//cols.put("FOREIGN KEY (player, id)", "REFERENCES persona(player, id) ON DELETE CASCADE");
		cols.put("UNIQUE (player, id, name)", "ON CONFLICT IGNORE");
		sqlHandler.createTable("persona_names", cols);

		cols = Maps.newLinkedHashMap();
		cols.put("race", "TEXT PRIMARY KEY");
		cols.put("world", "TEXT NOT NULL");
		cols.put("x", "INT NOT NULL");
		cols.put("y", "INT NOT NULL");
		cols.put("z", "INT NOT NULL");
		cols.put("yaw", "REAL");
		sqlHandler.createTable("persona_race_spawns", cols);

		//Blockregistry persistence stuff
		cols = Maps.newLinkedHashMap();
		cols.put("world", "TEXT NOT NULL");
		cols.put("x", "INT");
		cols.put("y", "INT");
		cols.put("z", "INT");
		cols.put("UNIQUE (world, x, y, z)", "ON CONFLICT IGNORE");
		sqlHandler.createTable("blockregistry", cols);

		sqlHandler.execute("DELETE FROM blockregistry WHERE ROWID IN (SELECT ROWID FROM blockregistry ORDER BY ROWID DESC LIMIT -1 OFFSET 5000)");

		try{
			ResultSet res = sqlHandler.query("SELECT * FROM blockregistry");
			while(res.next()){
				String world = res.getString(1);
				int x = res.getInt(2);
				int y = res.getInt(3);
				int z = res.getInt(4);
				WeakBlock wb = new WeakBlock(world, x, y, z);
				blockRegistry.playerPlaced.add(wb);
			}
			res.getStatement().close();
		}catch(SQLException e){e.printStackTrace();}


		Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            if(willCachePersonas()){
                long time = System.currentTimeMillis();
                getLogger().info("Preloading Personas as far back as " + cachePersonas + " days");
                personaHandler.initPreload(cachePersonas);
                getLogger().info("Personas were loaded in " + (System.currentTimeMillis() - time) + "ms.");
            }

            //Incase of a reload, load all Personas for currently logged in players
            //People that still reload deserve to be dunked in acid, though.
            for(Player p : Bukkit.getOnlinePlayers()){
                personaHandler.initPlayer(p);
            }

            //Start saving our data
            saverThread = new Thread(new DataSaveRunnable(saveHandler, timer, sqlHandler), "ArcheCore SQL Consumer");
            saverThread.start();

        });

		//Start tracking Personas and their playtime.
		new TimeTrackerRunnable(personaHandler).runTaskTimer(this, 2400, 1200);

		//Some racial bonus stuff
		/*		if(this.areRacialBonusesEnabled())
			new RacialBonusRunnable().runTaskTimer(this, 2400, 100);*/

		//Initialize some essential help topics
		initHelp();

		//Redirect Plugin commands
		initCommands();

		//Start all our Event listeners
		initListeners();

		//Init skilltome logging
		SkillTome.init(sqlHandler);

		//Init treasurechest logging
		TreasureChest.initSQL();

		//Create internally handled skill that holds Xp given from skill resets
		registerNewSkill("internal_drainxp")
		.withVisibilityType(Skill.VISIBILITY_HIDDEN)
		.withXpGainWhileHidden(true)
		.register();
	}

	private void initConfig(){
		//Initialize from config
		FileConfiguration config = getConfig(); // Get the config file either out of our .jar our datafolder
		saveDefaultConfig(); //Save the config file to disk if it doesn't exist yet.

		helpOverriden = config.getBoolean("override.help.command");
		legacyCommands = config.getBoolean("enable.legacy.commands");
		nameChangeDelay = config.getInt("name.change.delay");
		personaChangeDelay = config.getInt("persona.change.delay");
		showXpToPlayers = config.getBoolean("show.exp.values");
		racialBonuses = config.getBoolean("enable.racial.bonuses");
		enablePrefixes = config.getBoolean("enable.persona.prefix");
		modifyDisplayNames = config.getBoolean("modify.player.displayname");
		newbieProtectDelay = config.getInt("persona.newbie.protect");
		debugMode = config.getBoolean("enable.debug.mode");
		newbieDelay = config.getInt("newbie.notification");
		useWiki = config.getBoolean("enable.wiki.lookup");
		cachePersonas = config.getInt("persona.cache.time");
		protectiveTeleport = config.getBoolean("teleport.to.rescue");
		teleportNewbies = config.getBoolean("new.persona.to.spawn");
		worldName = config.getString("server.world.name");
		racialSwingTimer = config.getBoolean("racial.swing.timer");
		usingMySQL = config.getBoolean("enable.mysql");


		if(config.getBoolean("bonus.xp.racial"))
			ArcheSkillFactory.activateXpMod(ExpModifier.RACIAL);
		if(config.getBoolean("bonus.xp.playtime"))
			ArcheSkillFactory.activateXpMod(ExpModifier.PLAYTIME);
		if(config.getBoolean("bonus.xp.autoage"))
			ArcheSkillFactory.activateXpMod(ExpModifier.AUTOAGE);

		if(teleportNewbies){
			World w = Bukkit.getWorld(config.getString("preferred.spawn.world"));
			if(w != null) this.newbieWorldUUID = w.getUID();
			else getLogger().info("Could not find config-specified world. Will use default world instead.");
		}

		if(config.getBoolean("enable.economy"))
			economy = new ArcheEconomy(config);
	}

	private void initCommands(){
		getCommand("archehelp").setExecutor(new CommandArchehelp(helpdesk, helpOverriden));
		getCommand("helpmenu").setExecutor(new CommandHelpMenu(helpdesk));
		getCommand("persona").setExecutor(new CommandPersona(helpdesk, personaHandler, nameChangeDelay, enablePrefixes));
		getCommand("skill").setExecutor(new CommandSkill(helpdesk, showXpToPlayers));
		getCommand("beaconme").setExecutor(new CommandBeaconme());
		getCommand("treasurechest").setExecutor(new CommandTreasurechest());
		getCommand("realname").setExecutor(new CommandRealname(this));
		getCommand("autoage").setExecutor(new CommandAutoage(this));
		getCommand("money").setExecutor(new CommandMoney(helpdesk, economy));
		getCommand("namelog").setExecutor(new CommandNamelog());
		getCommand("arsql").setExecutor(new CommandSql());
		//501 added these
		getCommand("arclone").setExecutor(new CommandSqlClone());
		getCommand("newbies").setExecutor(new CommandNewbies(personaHandler));
		getCommand("racespawn").setExecutor(new CommandRaceSpawn(personaHandler));
		getCommand("attribute").setExecutor(new CommandAttribute());
		getCommand("attribute").setTabCompleter(new CommandAttributeTabCompleter());
	}

	private void initListeners(){
		PluginManager pm = Bukkit.getPluginManager();

		pm.registerEvents(new PlayerJoinListener(personaHandler), this);
		pm.registerEvents(new PlayerInteractListener(this), this);
		pm.registerEvents(new BeaconMenuListener(this, personaChangeDelay), this);
		pm.registerEvents(new HelpMenuListener(this, helpdesk), this);
		pm.registerEvents(new PlayerChatListener(), this);
		pm.registerEvents(new TreasureChestListener(), this);
		pm.registerEvents(new BlockRegistryListener(blockRegistry), this);
		pm.registerEvents(new JistumaCollectionListener(), this);
		pm.registerEvents(new DebugListener(), this);
		pm.registerEvents(new PersonaInventoryListener(), this);
		//if (permissions) pm.registerEvents(new PersonaPermissionListener(personaHandler.getPermHandler()), this);

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

		//Set the essential Help files

		final String div = ChatColor.LIGHT_PURPLE +  "\n--------------------------------------------------\n";

		String persHelp = ChatColor.YELLOW + "Your " + ChatColor.ITALIC + "Persona" + ChatColor.YELLOW + " is an uncouth sailor, fair Elven maiden or parent-slaying Orc. " +
				"in Lord of the Craft, you speak and act as your current Persona, and know only what they know." +
				div + ChatColor.GREEN + "Once accepted, creating your Persona is the first step of your adventure."
				+ " You can later remove or create new Personas, by finding a " + ChatColor.ITALIC + "@Beacon@"
				+ ChatColor.GREEN + " and right-clicking it.";

		String commandHelp = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Your essential commands are as follows: "
				+ div + ChatColor.GRAY + "" + ChatColor.ITALIC +  (helpOverriden? "$/help$":"$/archehelp$") + ChatColor.GRAY + ": Provides a useful database of help topics.\n"
				+ ChatColor.GOLD + "" + ChatColor.ITALIC + "$/helpmenu$" + ChatColor.GOLD + ": The same help topics, provided in a menu form.\n"
				+ ChatColor.BLUE + "" + ChatColor.ITALIC + "$/persona$" + ChatColor.BLUE + ": See others' Personas and modify your own.\n"
				+ ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "$/skill$" + ChatColor.LIGHT_PURPLE + ": See the Skills your Persona can learn"
				+ ChatColor.DARK_GREEN+ "" + ChatColor.ITALIC + "$/me$"+ChatColor.DARK_GREEN+": Opens the nexus-menu";

		String beaconInfo = "A " + ChatColor.ITALIC + "Beacon" + ChatColor.RESET + " lets you control most Persona-related tasks. Finding and right-clicking a beacon"
				+ " grants access to, among other things, Persona modification and the plugin help files (which are also accessible via @commands@). Knowing where Beacons are located will be crucial for "
				+ " orienting yourself with the ArcheCore plugin.";

		String profession = ChatColor.GRAY + "/sk [skill] select {main/second/bonus}: " + ChatColor.GOLD + "Select a profession.\n"
				+ ChatColor.GREEN + "You have to select a profession to raise that skill to the maximum possible tier. You can, however, experiment with any skill before committing to a profession.\n"
				+ ChatColor.BLUE  + "Depending on your Persona's race, you might also have a Racial Skill. Racial skills can be freelly leveled and do not need to be selected.\n"
				+ ChatColor.GRAY  + "Normally, you can be Adequate in any non-selected skill. If you pick a primary profession, this cap is tightened down to let you be Clumsy. If you also pick a secondary, your other skills will be locked at the Inept tier.";

		addHelp("Persona", persHelp, Material.REDSTONE_COMPARATOR);
		addHelp("Commands", commandHelp, Material.COMMAND);
		addHelp("Professions", profession, Material.BEDROCK);
		addInfo("Beacon", beaconInfo);

		//Create config-set Help Files
		if(!(new File(getDataFolder(), "helpfiles.yml").exists()))
			saveResource("helpfiles.yml", false);
		FileConfiguration c = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "helpfiles.yml"));
		for(String key : c.getKeys(false)){
			if(c.isConfigurationSection(key)){
				ConfigurationSection section = c.getConfigurationSection(key);
				String name = section.isString("topic")? section.getString("topic") : key;

				String desc = null;
				if (section.isString("content")) desc = section.getString("content").replace('&', ChatColor.COLOR_CHAR);
				else continue;

				if(section.isString("icon")){
					try{
						Material m = Material.valueOf(section.getString("icon"));
						addHelp(name, desc, m);
					}catch(IllegalArgumentException e){
						addHelp(name, desc);
					}
				} else addHelp(name, desc);
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

				String desc = null;
				if (section.isString("content")) desc = section.getString("content").replace('&', ChatColor.COLOR_CHAR);
				else continue;

				addInfo(name, desc);
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

	public ArcheTimer getMethodTimer(){
		return timer;
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
	public SQLHandler getSQLHandler(){
		return sqlHandler;
	}

	@Override
	public void addHelp(HelpFile helpfile){
		helpdesk.addTopic(helpfile);
	}

	@Override
	public void addHelp(String topic, String output){
		helpdesk.addTopic(topic, output);
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
	public Skill getSkill(String skillName){
		return ArcheSkillFactory.getSkill(skillName);
	}

	@Override
	public Skill createSkill(String skillName){
		try {
			return ArcheSkillFactory.createSkill(skillName);
		} catch (DuplicateSkillException e) {
			getLogger().severe("Duplicate skill detected: " + skillName);
			return ArcheSkillFactory.getSkill(skillName);
		}
	}

	@Override
	public SkillFactory registerNewSkill(String skillName){
		try {
			return ArcheSkillFactory.registerNewSkill(skillName);
		} catch (DuplicateSkillException e) {
			getLogger().severe("Duplicate skill detected: " + skillName);
			return null;
		}
	}

    @Override
	public void setShouldClone(boolean val) { this.shouldClone = val; }

    @Override
    public boolean isCloning() { return shouldClone; }

	@Override
	public ItemStack giveSkillTome(Skill skill){
		return SkillTome.giveTome(skill);
	}

	@Override
	public ItemStack giveTreasureChest(){
		return TreasureChest.giveChest();
	}

	@Override
	public boolean areRacialBonusesEnabled(){
		return racialBonuses;
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

	public boolean willCachePersonas(){
		return cachePersonas > 0;
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
	public Economy getEconomy() {
		return economy;
	}

	@Override
	public JMisc getMisc() { return jcoll; }

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
	public boolean isRacialSwingEnabled() {
		return racialSwingTimer;
	}
}