package net.lordofthecraft.arche.interfaces;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

import net.lordofthecraft.arche.BlockRegistry;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.help.HelpFile;
import net.lordofthecraft.arche.seasons.LotcianCalendar;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import net.lordofthecraft.arche.skin.SkinCache;

public interface IArcheCore {

	/**
	 * Persona retrieval and loading methods are done by the PersonaHandler.
	 * @return The PersonaHandler singleton
	 */
	PersonaHandler getPersonaHandler();
	
	/**
	 * Fatigue of Personas ties into skills and is done by the FatigueHandler
	 * @return The FatigueHandler singleton
	 */
	FatigueHandler getFatigueHandler();

	/**
	 * Each Persona is uniquely identified with a composite key that consists of
	 * the Mojang Player UUID and a integer that refers to the Persona of the player.
	 * The PersonaKey pairs these two objects, and can be used to compare Personas and as keys in HashMaps.
	 * @param uuid UUID of the player
	 * @param pid Id of the persona
	 * @return A Persona Key constructed on the criteria
	 */
	PersonaKey composePersonaKey(UUID uuid, int pid);

    boolean logsPersonaDeletions();

    /**
     * ArcheCore maintains a temporary non-persistent registry of all player-placed blocks.
	 * Use it to keep track of which blocks have been placed by players and should not provide Skill Xp for this reason.
	 * @return The active Block Registry
	 */
	BlockRegistry getBlockRegistry();
	

	/**
	 * the ArcheSQLiteHandler provides a series of synchronized methods for communicating with the
	 * underlying SQLite database
	 * @return The sqlHandler object used by ArcheCore and its plugins
	 */
	SQLHandler getSQLHandler();

    /**
     * The Consumer is the SQL processor which safely and efficiently saves SQL data for Personas.
     *
     * @return The Consumer object
     */
    IConsumer getConsumer();

    /**
     * Adds a Topic to the HelpDesk, revealing it to all players via the GUI and command
	 * If already instantiated, you can add a HelpFile object directly 
	 * rather than have the HelpDesk construct it. 
	 * @param helpfile the HelpFile object that you want to add
	 */
	void addHelp(HelpFile helpfile);

	/**
	 * Adds a Topic to the HelpDesk, revealing it to all players via the GUI and command
	 * @param topic the topic to add
	 * @param output the text provided to the player when this topic is accessed
	 */
	void addHelp(String topic, String output);

    /**
     * Adds a Topic to the HelpDesk, revealing it to all players via the GUI and command
     *
     * @param topic      the topic to add
     * @param output     the text provided to the player when this topic is accessed
     * @param permission The permission needed to view this topic
     */
    void addHelp(String topic, String output, String permission);

	/**
	 * Adds a Topic to the HelpDesk, revealing it to all players via the GUI and command
	 * @param topic the topic to add
	 * @param output the text provided
	 * @param icon the clickable item to display on the help GUI
	 */
	void addHelp(String topic, String output, Material icon);

    /**
     * Adds a Topic to the HelpDesk, revealing it to all players via the GUI and command
     *
     * @param topic      the topic to add
     * @param output     the text provided
     * @param icon       the clickable item to display on the help GUI
     * @param permission The permission needed to read this help topic
     */
    void addHelp(String topic, String output, Material icon, String permission);

	/**
	 * Adds a Topic to the HelpDesk as an info topic, allowing linking to the topic but not revealing it
	 * @param topic the info topic to add
	 * @param output the text provided
	 */
	void addInfo(String topic, String output);

    /**
     * Adds a Topic to the HelpDesk as an info topic, allowing linking to the topic but not revealing it
     *
     * @param topic      the info topic to add
     * @param output     the text provided
     * @param permission The permission needed to view this help topic
     */
    void addInfo(String topic, String output, String permission);

	/**
	 * Method to retrieve an existing Skill object
	 * @param skillName The skill you want to retrieve
	 * @return The skill associated with the given name, or null if not found
	 */
	Skill getSkill(String skillName);

	/**
	 * Method to create and register a new Skill with the default settings. This
	 * skill must not yet exist. Use {@link #getSkill(String)} to retrieve an already registered Skill
	 * @param skillName The name of the Skill to be created.
     * @param controller The plugin which is registering this skill and will be controlling it's implementation
     * @return the Created Skill
     * @throws ArcheSkillFactory.DuplicateSkillException If a skill with the same name already exists
     */
    Skill createSkill(String skillName, Plugin controller);

	/**
	 * Method to create a Skill with arbitrary settings, each of them initialised
	 * with the help of the SkillFactory object.
	 * @param skillName The name of the Skill to be created.
     * @param controller The plugin which is registering this skill and will be controlling it's implementation
     * @return the constructed SkillFactory object
     * @throws ArcheSkillFactory.DuplicateSkillException If a skill with the same name already exists
     */
    SkillFactory registerNewSkill(String skillName, Plugin controller);

    /**
     * Sets if ArcheCore will attempt to clone it's database on a restart
     * @param val The flag to clone or not
     */

	void setShouldClone(boolean val);

    /**
     * @return If ArcheCore has a DB clone scheduled
     */
	boolean isCloning();

	/** 
	 * If enabled, ArcheCore gives each of the races defined in its Method some 
	 * unique stat bonuses to make each race unique.
	 * @return Whether or not racial bonuses are enabled.
	 */
	boolean areRacialBonusesEnabled();

	/**
	 * If enabled, ArcheCore lets Players set a modifiable prefix before their Persona Name
	 * This prefix is unique per Persona and immediately modifiable.
	 * @return Whether or not prefixes are enabled.
	 */
	boolean arePrefixesEnabled();

	/**
	 * If enabled, new Personas receive Newbie protection which exempts them from mechanical PvP for a set period of playtime
	 * @return Minutes of PvP invulnerability time, in minutes 
	 */
	int getNewbieProtectDelay();

	/**
	 * If true, ArcheCore will modify display names of Players to fit their current Persona's name
	 * @return If display names are to be modified by ArcheCore
	 */
	boolean willModifyDisplayNames();

	/**
	 * Check with the Block Registry if the player placed a certain (watched) block this server session
	 * @param b The block to check
	 * @return If the called block was placed by a player this session.
	 */
	boolean isBlockPlayerPlaced(Block b);
	
	/**
	 * ArcheCore optionally has functionality for money tied to specific Personas. This boolean tells you if it is used 
	 * @return Whether Economy is enabled
	 */
	boolean usesEconomy();

	/**
	 * The Calendar is ArcheCore's timekeeping method to regulate minecraft's day/night cycle as well as seasons
	 * Personas age in accordance with this calendar.
	 * @return The LotcianCalendar running with ArcheCore
	 */
	LotcianCalendar getCalendar();
	
	/**
	 * the Economy objects holds some of the customizable settings of the ArcheCore economy
	 * @return The Economy object
	 */
	Economy getEconomy();

	/**
	 * @return If new Personas will be teleported to spawn after creation
	 */
	boolean teleportNewPersonas();
	
	/**
	 * @return A preferred world for new Personas to end up in, if available
	 */
	World getNewPersonaWorld();

	/**
	 * @return The new persona shield delay, if larger than 0
	 */
	int getNewbieDelay();

    boolean canCreatePersonas();

    /**
     * @return the delay in days within which newly created Personas cannot be permakilled by their owners.
	 */
	int getNewPersonaPermakillDelay();

	/**
	 * @return The current name of the world of LotC
	 */

	String getServerWorldName();

	/**
	 * @return Whether or not the Persona menu will have a button in the menu
	 */
	boolean showEnderchestInMenu();

	/**
	 * @return If damage bonuses are enabled for races
	 */
	boolean areRacialDamageBonusesEnabled();

	/**
	 * Skin Cache keeps track of skins belonging to players, and custom skins applied to persona
	 * @return The SkinCache singleton
	 */
	SkinCache getSkinCache();

	/**
	 * How much persona slots a player is able to unlock.
	 * amount of slots that are locked is dependent on permission nodes instead.
	 * @return maximum number of persona slots
	 */
	int personaSlots();

    /**
     * Checks whether or not ArcheCore is using SQLite.
     *
     * @return If the SQLhandler is an instance of {@link net.lordofthecraft.arche.SQL.ArcheSQLiteHandler}
     */
    boolean isUsingSQLite();
}