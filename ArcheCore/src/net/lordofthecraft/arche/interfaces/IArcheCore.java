package net.lordofthecraft.arche.interfaces;

import java.util.UUID;

import net.lordofthecraft.arche.BlockRegistry;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.help.HelpFile;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public interface IArcheCore {

	/**
	 * Persona retrieval and loading methods are done by the PersonaHandler.
	 * @return The PersonaHandler singleton
	 */
	public PersonaHandler getPersonaHandler();
	
	/**
	 * Each Persona is uniquely identified with a composite key that consists of
	 * the Mojang Player UUID and a integer that refers to the Persona of the player.
	 * The PersonaKey pairs these two objects, and can be used to compare Personas and as keys in HashMaps.
	 * @param uuid
	 * @param pid
	 * @return A Persona Key constructed on the criteria
	 */
	public PersonaKey composePersonaKey(UUID uuid, int pid);
	
	/**
	 * ArcheCore maintains a temporary non-persistent registry of all player-placed blocks.
	 * Use it to keep track of which blocks have been placed by players and should not provide Skill Xp for this reason.
	 * @return The active Block Registry
	 */
	public BlockRegistry getBlockRegistry();
	

	/**
	 * the SQLHandler provides a series of synchronized methods for communicating with the
	 * underlying SQLite database
	 * @return The sqlHandler object used by ArcheCore and its plugins
	 */
	public SQLHandler getSQLHandler();

	/**
	 * Adds a Topic to the HelpDesk, revealing it to all players via the GUI and command
	 * If already instantiated, you can add a HelpFile object directly 
	 * rather than have the HelpDesk construct it. 
	 * @param helpfile the HelpFile object that you want to add
	 */
	public void addHelp(HelpFile helpfile);

	/**
	 * Adds a Topic to the HelpDesk, revealing it to all players via the GUI and command
	 * @param topic the topic to add
	 * @param output the text provided to the player when this topic is accessed
	 */
	public void addHelp(String topic, String output);

	/**
	 * Adds a Topic to the HelpDesk, revealing it to all players via the GUI and command
	 * @param topic the topic to add
	 * @param output the text provided
	 * @param icon the clickable item to display on the help GUI
	 */
	public void addHelp(String topic, String output, Material icon);

	/**
	 * Adds a Topic to the HelpDesk as an info topic, allowing linking to the topic but not revealing it
	 * @param topic the info topic to add
	 * @param output the text provided
	 */
	public void addInfo(String topic, String output);

	/**
	 * Method to retrieve an existing Skill object
	 * @param skillName The skill you want to retrieve
	 * @return The skill associated with the given name, or null if not found
	 */
	public Skill getSkill(String skillName);

	/**
	 * Method to create and register a new Skill with the default settings. This
	 * skill must not yet exist. Use {@link #getSkill(String)} to retrieve an already registered Skill
	 * @param skillName The name of the Skill to be created.
	 * @return the Created Skill
	 */
	public Skill createSkill(String skillName);

	/**
	 * Method to create a Skill with arbitrary settings, each of them initialised
	 * with the help of the SkillFactory object.
	 * @param skillName The name of the Skill to be created.
	 * @return the constructed SkillFactory object
	 */
	public SkillFactory registerNewSkill(String skillName);

	/**
	 * Convenience method to create a Skill Tome for the specified skill.
	 * Players can consume skill tomes at beacons for an XP boost in the given skill.
	 * @param skill the skill for which you want this Skill Tome to work.
	 * @return The Skill Tome itemstack
	 */
	public ItemStack giveSkillTome(Skill skill);

	/**
	 * Convenience method to Create a Treasure Chest which draws from a predefined Loot
	 * Table in the ArcheCore plugin. Treasure Chests can be opened by all players to
	 * retrieve an item reward (1-2 items)
	 * @return The Treasure Chest item
	 */
	public ItemStack giveTreasureChest();

	/** 
	 * If enabled, ArcheCore gives each of the races defined in its Method some 
	 * unique stat bonuses to make each race unique.
	 * @return Whether or not racial bonuses are enabled.
	 */
	public boolean areRacialBonusesEnabled();

	/**
	 * If enabled, ArcheCore lets Players set a modifiable prefix before their Persona Name
	 * This prefix is unique per Persona and immediately modifiable.
	 * @return Whether or not prefixes are enabled.
	 */
	public boolean arePrefixesEnabled();

	/**
	 * If enabled, new Personas receive Newbie protection which exempts them from mechanical PvP for a set period of playtime
	 * @return Minutes of PvP invulnerability time, in minutes 
	 */
	public int getNewbieProtectDelay();

	/**
	 * If true, ArcheCore will modify display names of Players to fit their current Persona's name
	 * @return If display names are to be modified by ArcheCore
	 */
	public boolean willModifyDisplayNames();

	/**
	 * Check with the Block Registry if the player placed a certain (watched) block this server session
	 * @param b The block to check
	 * @return If the called block was placed by a player this session.
	 */
	public boolean isBlockPlayerPlaced(Block b);
	
	/**
	 * Checks if ArcheCore is instructed to preload Personas of players that logged in within a certain time.
	 * @return If ArcheCore is instructed to preload Personas
	 */
	public boolean willCachePersonas();
	
	/**
	 * ArcheCore optionally has functionality for money tied to specific Personas. This boolean tells you if it is used 
	 * @return Whether Economy is enabled
	 */
	public boolean usesEconomy();
	
	/**
	 * the Economy objects holds some of the customizable settings of the ArcheCore economy
	 * @return The Economy object
	 */
	public Economy getEconomy();
	
	/**
	 * @return If new Personas will be teleported to spawn after creation
	 */
	public boolean teleportNewPersonas();
	
	/**
	 * @return A preferred world for new Personas to end up in, if available
	 */
	public World getNewPersonaWorld();

}