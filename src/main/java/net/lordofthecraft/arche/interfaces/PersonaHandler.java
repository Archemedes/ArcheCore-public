package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;

public interface PersonaHandler {
    String personaSelect = "SELECT " +
            "persona_id,slot,race_key,gender" +
            ",name,curr,race_header,descr,prefix,money,skin,profession,fatigue,max_fatigue" +
            ",world,x,y,z,inv,ender_inv,health,hunger,saturation,creature" +
            ",played,chars,renamed,playtime_past,date_created,last_played " +
            "FROM persona JOIN persona_vitals ON persona.persona_id=persona_vitals.persona_id_fk " +
            "JOIN persona_stats ON persona.persona_id=persona_stats.persona_id_fk " +
            "WHERE player_fk=?";

	/**
	 * @return If archecore is currently preloading personas
	 */

	boolean isPreloading();

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
	 * Check the maximum amount of Personas a Player is allowed to have. Between 0-4
	 * @param p Player to check for
	 * @return Amount of Personas Player may have, based on Permission Nodes
	 */
	int getAllowedPersonas(Player p);

    /**
     * Retrieve an <b>unmodifiable</b> collection of all active personas.
     *
     * @return A <b>unmodifiable</b> collection of personas which are loaded.
     */
    Collection<ArchePersona[]> getPersonas();

	/**
	 * Attempts to find a Persona uniquely corresponding to a Persona Key. Persona may not exist or may not be loaded
	 * @param key The PersonaKey to look with
	 * @return the Persona (null if not found)
	 */
	Persona getPersona(PersonaKey key);

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

    Optional<ArchePersona> getPersona(int persona_id);

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
	 * Returns all of the Player's current Personas. Each persona's ID should correspond
	 * to the index of the Persona in the returned Array.
	 * @param p The Player in question
	 * @return An array of the Player's persona.
	 */
	Persona[] getAllPersonas(OfflinePlayer p);

	/**
	 * Returns all of the Player's current Personas. Each persona's ID should correspond
	 * to the index of the Persona in the returned Array.
	 * @param uuid The UUID of the Player in question
	 * @return An array of the Player's persona.
	 */
	Persona[] getAllPersonas(UUID uuid);

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
	 * Make a Player switch his current Persona. Players may have 4 personas contained in an array of length 4
	 * @param p Player in question
	 * @param id the ID of the persona, between 0-3, corresponding to the Persona array index
	 * @return If the switch was successful
	 */
	boolean switchPersona(Player p, int id);

	/**
	 * Method that provides a human-readable list of information about a Persona, to be used in prints
	 * @param p the Persona to be looked up
	 * @param mod If the user viewing is a moderator
	 * @return A list of initialised stats of the given Persona
	 */
	List<BaseComponent> whois(Persona p, boolean mod);

	/**
	 * Method that provides a human-readable list of information about a
	 * Player's current Persona, to be used in prints
	 * @param p the Player to be looked up
	 * @param mod If the user viewing is a moderator
	 * @return A list of initialised stats of the given Persona
	 */
	List<BaseComponent> whois(Player p, boolean mod);

	/**
	 * Method that provides a human-readable list of additional information, to be used in prints
	 * @param p the Persona to be looked up
	 * @param mod If the user viewing is a moderator
	 * @param self If the user viewing is the owner of the persona
	 * @return A list of initialised stats of the given Persona
	 */

	List<BaseComponent> whoisMore(Persona p, boolean mod, boolean self);

	/**
	 * Gets the value of the luck attribute for a player
	 *
	 * @param p The player to check
	 * @return The double value of the attribute, 0.0 if none found or none assigned
	 */

	double getLuck(@Nonnull Player p);

	/**
	 * Gets the races which have unique spawns assigned to them
	 *
	 * @return An unmodifiable list of Race and Location
	 */
	Map<Race, Location> getRacespawns();
}