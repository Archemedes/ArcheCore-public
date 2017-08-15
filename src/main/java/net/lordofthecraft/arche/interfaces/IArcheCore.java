package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.BlockRegistry;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.help.HelpFile;
import net.lordofthecraft.arche.skill.BonusExpModifierHandler;
import net.lordofthecraft.arche.skin.SkinCache;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface IArcheCore {

	/**
	 * Persona retrieval and loading methods are done by the PersonaHandler.
	 * @return The PersonaHandler singleton
	 */
	PersonaHandler getPersonaHandler();
	
	/**
	 * Each Persona is uniquely identified with a composite key that consists of
	 * the Mojang Player UUID and a integer that refers to the Persona of the player.
	 * The PersonaKey pairs these two objects, and can be used to compare Personas and as keys in HashMaps.
	 * @param uuid UUID of the player
	 * @param pid Id of the persona
	 * @return A Persona Key constructed on the criteria
	 */
	PersonaKey composePersonaKey(UUID uuid, int pid);
	
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
	 * @param topic the topic to add
	 * @param output the text provided
	 * @param icon the clickable item to display on the help GUI
	 */
	void addHelp(String topic, String output, Material icon);

	/**
	 * Adds a Topic to the HelpDesk as an info topic, allowing linking to the topic but not revealing it
	 * @param topic the info topic to add
	 * @param output the text provided
	 */
	void addInfo(String topic, String output);

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
	 * @return the Created Skill
	 */
	Skill createSkill(String skillName);

	/**
	 * Method to create a Skill with arbitrary settings, each of them initialised
	 * with the help of the SkillFactory object.
	 * @param skillName The name of the Skill to be created.
	 * @return the constructed SkillFactory object
	 */
	SkillFactory registerNewSkill(String skillName);

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
	 * Convenience method to create a Skill Tome for the specified skill.
	 * Players can consume skill tomes at beacons for an XP boost in the given skill.
	 * @param skill the skill for which you want this Skill Tome to work.
	 * @return The Skill Tome itemstack
	 */
	ItemStack giveSkillTome(Skill skill);

	/**
	 * Convenience method to Create a Treasure Chest which draws from a predefined Loot
	 * Table in the ArcheCore plugin. Treasure Chests can be opened by all players to
	 * retrieve an item reward (1-2 items)
	 * @return The Treasure Chest item
	 */
	ItemStack giveTreasureChest();

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
	 * Checks if ArcheCore is instructed to preload Personas of players that logged in within a certain time.
	 * @return If ArcheCore is instructed to preload Personas
	 */
	boolean willCachePersonas();
	
	/**
	 * ArcheCore optionally has functionality for money tied to specific Personas. This boolean tells you if it is used 
	 * @return Whether Economy is enabled
	 */
	boolean usesEconomy();
	
	/**
	 * the Economy objects holds some of the customizable settings of the ArcheCore economy
	 * @return The Economy object
	 */
	Economy getEconomy();
	
	/**
	 * JMisc holds a set of methods and values for the LotC coder Jistuma, implemented here for easy access.
	 * @return the JMisc object
	 */
	JMisc getMisc();
	
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
	
	/**
	 * @return The current name of the world of LotC
	 */

	String getServerWorldName();

	/**
	 * @return If the racial swing timers are enabled
	 */
	boolean isRacialSwingEnabled();

	
	/**
	 * @return The bonus exp modifier handler
	 */
	BonusExpModifierHandler getBonusExpModifierHandler();

	/**
	 * @return If damage bonuses are enabled for races
	 */
	
	boolean areRacialDamageBonusesEnabled();
	
	/**
	 * Skin Cache keeps track of skins belonging to players, and custom skins applied to persona
	 * @return The SkinCache singleton
	 */
	SkinCache getSkinCache();
}