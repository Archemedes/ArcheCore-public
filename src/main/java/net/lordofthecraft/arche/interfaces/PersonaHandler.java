package net.lordofthecraft.arche.interfaces;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.lordofthecraft.arche.account.Waiter;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.persona.ArcheOfflinePersona;
import net.md_5.bungee.api.chat.BaseComponent;

public interface PersonaHandler {

	/**
	 * Set whether or not ArcheCore should continue to modify Bukkit Display Names to reflect Persona names
	 * @param will the value to set to
	 */
	void setModifyDisplayNames(boolean will);

	/**
	 * If true, ArcheCore will modify display names of Players to fit their current Persona's name
	 * @return If display names are to be modified by ArcheCore
	 */
	boolean willModifyDisplayNames();

	/**
	 * See if Player is allowed to use the ArcheCore persona system
	 * @param p Player to check for
	 * @return Player has permission node archecore.mayuse
	 */
	boolean mayUse(Player p);

	/**
	 * Check the maximum amount of Personas a Player is allowed to have.
	 * This can be between 0 and 17 but depends on the config setting for max personas.
	 * @param p Player to check for
	 * @return Amount of Personas Player may have, based on Permission Nodes
	 */
	int getAllowedPersonas(Player p);

    /**
     * Retrieve an <b>unmodifiable</b> collection of all active personas.
     *
     * @return A <b>unmodifiable</b> collection of personas which are loaded.
     */
    Collection<ArcheOfflinePersona> getPersonas();

	/**
	 * Attempts to find a Persona corresponding to a Player UUID and id. Persona may not exist or may not be loaded
	 * @param uuid The Player UUID to look for
	 * @param id The internal persona id 0-3
	 * @return The found Persona (null if not found)
	 */
	Persona getPersona(UUID uuid, int id);

	/**
	 * Fetch a Player's current Persona
	 * @param p Player in question
	 * @return Current Persona, or 'null' if not found.
	 */
	Persona getPersona(OfflinePlayer p);
	
	/**
	 * Method that will return an (Offline)Persona, the player's current Persona, regardless of player login status
	 * @param player Player UUID
	 * @return Player's current Persona, or null if no current persona can be found
	 */
	OfflinePersona getOfflinePersona(UUID player);
	
	/**
	 * Method that will return an (Offline)Persona, at a given slot, regardless of player login status
	 * @param player Player UUID
	 * @param slot the persona slot
	 * @return Player's current Persona, or null if no current persona can be found
	 */
	OfflinePersona getOfflinePersona(UUID player, int slot);

	/**
	 * Gets the person by the <b>Persona ID</b> - This <i>is not</i> a player UUID. Persona UUID is unique to each persona
	 *
	 * @param persona_id The int id of this persona
	 * @return The Persona which is wrapped in an Optional
	 */
	OfflinePersona getPersonaById(int persona_id);

    /**
     * Fetch a Player's current Persona
	 * @param p Player in question
	 * @return Current Persona, or 'null' if not found.
	 */
	Persona getPersona(Player p);


	/**
	 * Returns whether or not the given Player currently has an
	 * active persona registered and loaded. In other words, it sees whether or not
	 * the {@link #getPersona(Player)} method does not return 'null'
	 * @param p The Player in question
	 * @return If the Player has a current Persona
	 */
	boolean hasPersona(Player p);

	/**
	 * Returns all of the Player's Personas.
	 * @param p The Player in question
	 * @return An array of the Player's persona.
	 */
	Collection<Persona> getPersonasUnordered(Player p);
	
	/**
	 * Returns all of the Player's Personas.
	 * Personas might be either off- or online variant of the Persona.
	 * @param p The OfflinePlayer in question
	 * @return An array of the Player's persona.
	 */
	Collection<OfflinePersona> getPersonasUnordered(OfflinePlayer p);
	
	/**
	 * Returns all of the Player's Personas.
	 * Personas might be either off- or online variant of the Persona.
	 * @param uuid UUID of player
	 * @return An array of the Player's persona.
	 */
	Collection<OfflinePersona> getPersonasUnordered(UUID uuid);
	
	/**
	 * Returns all of the Player's Personas. Each persona's ID should correspond
	 * to the index of the Persona in the returned Array.
	 * @param p The Player in question
	 * @return An array of the Player's persona.
	 */
	Persona[] getAllPersonas(Player p);
	
	/**
	 * Returns all of the OfflinePlayer's Personas. Each persona's ID should correspond
	 * to the index of the Persona in the returned Array.
	 * Personas might be either off- or online variant of the Persona.
	 * @param p The OfflinePlayer in question
	 * @return An array of the Player's OfflinePersonas.
	 */
	OfflinePersona[] getAllPersonas(OfflinePlayer p);

	/**
	 * Returns all of the Player's current Personas. Each persona's ID should correspond
	 * to the index of the Persona in the returned Array.
	 * @param uuid The UUID of the Player in question
	 * @return An array of the Player's persona.
	 */
	Persona[] getAllPersonas(UUID uuid);
	
	/**
	 * Returns all of a Player's Personas. Each persona's ID should correspond
	 * to the index of the Persona in the returned Array.
	 * Personas might be either off- or online variant of the Persona.
	 * @param uuid The UUID of the Player in question (needn't be online)
	 * @return An array of the Player's OfflinePersonas.
	 */
	OfflinePersona[] getAllOfflinePersonas(UUID uuid);

	/**
	 * Gives the amount of Personas the current player possesses. This only counts
	 * Personas that are currently registered and selectable, not previous (killed) personas.
	 * @param uuid UUID of the player in question
	 * @return The amount of registered Personas
	 */
	int countPersonas(UUID uuid);

	/**
	 * @return all currently active personas in game
	 */

	List<Persona> getAllActivePersonas();

	/**
	 * Gives the amount of Personas the current player possesses. This only counts
	 * Personas that are currently registered and selectable, not previous (killed) personas.
	 * @param p Player in question
	 * @return The amount of registered Personas
	 */
	int countPersonas(Player p);

	/**
	 * Make a Player switch his current Persona.
	 * @param p Player in question
	 * @param id the ID of the persona, between 0-maxSlots(), corresponding to the Persona array index
	 * @param force Force the switch, ignoring the SwitchEvent's cancelled status
	 * @return If the switch was successful
	 */
	boolean switchPersona(Player p, int id, boolean force);

	/**
	 * @param op Take a wild fucking guess
	 * @return Object with callback possibility, which might execute instantly if the Persona already exists
	 */
	Waiter<Persona> loadPersona(OfflinePersona op);
	
	/**
	 * Method that provides a human-readable list of information about a Persona, to be used in prints
	 * @param p the Persona to be looked up
	 * @param whosAsking the CommandSender querying for persona info
	 * @return A list of initialised stats of the given Persona
	 */
    List<BaseComponent> whois(OfflinePersona p, CommandSender whosAsking);

	/**
	 * Method that provides a human-readable list of information about a
	 * Player's current Persona, to be used in prints
	 * @param p the Player to be looked up
	 * @param whosAsking Player querying for persona info
	 * @return A list of initialised stats of the given Persona
	 */
	List<BaseComponent> whois(Player p, Player whosAsking);

	/**
	 * Method that provides a human-readable list of additional information, to be used in prints
	 * @param p the Persona to be looked up
	 * @param whosAsking the CommandSender querying for persona info
	 * @return A list of initialised stats of the given Persona
	 */

    List<BaseComponent> whoisMore(OfflinePersona p, CommandSender whosAsking);

	/**
	 * Gets the races which have unique spawns assigned to them
	 *
	 * @return An unmodifiable list of Race and Location
	 */
	Map<Race, Location> getRacespawns();

	/**
	 * Register a new Persona with the ArcheCore system. They will be registered for the player with the given UUID
	 * @param persona Persona to register
	 */
	void registerPersona(Persona persona);
}