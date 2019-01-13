package net.lordofthecraft.arche.interfaces;

import java.sql.Timestamp;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

import net.lordofthecraft.arche.account.Waiter;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;

//Strictly no setters to be added to this interface.
//It fucks up persona loading. thanks. -Sporadic
public interface OfflinePersona {

    /**
     * Retrieve the persona-specific integer that uniquely defines this persona.
     *
     * @return The immutable int persona id
     */
    int getPersonaId();

    /**
     * Returns the session-invariant ID of the Persona. Ids are between 0 and 15.
     * IDs are only unique for the same player
     *
     * @return the immutable slot of the persona
     */
    int getSlot();

    /**
     * See if the Persona is a Player's current Persona
     *
     * @return Whether or not the Persona is current
     */
    boolean isCurrent();

    /**
     * Retrive the name of the Player that this Persona belongs to.
     *
     * @return the name of the owning player.
     */
    String getPlayerName();

    /**
     * Each Persona is uniquely identified with a composite key that consists of
     * the Mojang Player UUID and a integer that refers to the Persona of the player.
     * The PersonaKey pairs these two objects, and can be used to compare Personas and as keys in HashMaps.
     *
     * @return The Persona Key for this persona
     */
    PersonaKey getPersonaKey();

    /**
     * Retrieves the Type of this persona.
     *
     * @return The type of persona this is
     */
    PersonaType getPersonaType();

    /**
     * @return Persona's year of birth
     */
    int getDateOfBirth();
    
    /**
     * @return Persona's age
     */
    int getAge();
    
    /**
     * Get the human-readable Gender of this Persona
     *
     * @return The Persona's gender
     */
    String getGender();

    /**
     * @return getGender().equals("Male")
     */
    boolean isMale();

    /**
     * @return getGender().equals("Female")
     */
    boolean isFemale();

    /**
     * Retrieve the Roleplay name of this Persona
     *
     * @return The Persona's RP name.
     */
    String getName();

    /**
     * Retrieve the immutable Race of this Persona.
     *
     * @return Race of the Persona.
     */
    Race getRace();

    /**
     * Retrieve the Mojang-issued UUID coupled to this Persona's player
     *
     * @return the Player's unique id.
     */
    UUID getPlayerUUID();
    
    /**
     * @return the creation time of this persona in milliseconds
     */
    Timestamp getCreationTime();

    /**
     * Check if this persona is "loaded"
     *
     * @return Whether or not this is a loaded persona
     */
    boolean isLoaded();

    /**
     * @return Object with callback possibility, which might execute instantly if the Persona already exists
     */
    Waiter<Persona> load();
    
    /**
     * Get the loaded persona object, if applicable.
     * @see #isLoaded()
     * @return The loaded persona.
     */
    Persona getPersona();

    /**
     * Delete the Persona from the Plugin records.
     */
    void remove();

    /**
     * Persona tags are additional data that is automatically made persistent by ArcheCore's SQL backend
     * Dependent plugins can set and check tags for each Persona. Tags can also be modified by command.
     * Persona must be loaded (i.e. instanceof ArchePersona) for modifying methods to be successful.
     *
     * @return The Persona-bound tag handler.
     */
    PersonaTags tags();

    /**
     * @return The Player this Persona belongs to
     */
    OfflinePlayer getOfflinePlayer();
    
    /**
     * Gets the player-readable string of the current state of this Persona's races.
     *
     * @param mod Whether or not the string should be tailored around a moderator (and include hidden elements)
     * @return The formatted String with {@link org.bukkit.Color}s, or an empty string if the persona is {@link net.lordofthecraft.arche.enums.Race#UNSET} with nothing else
     */
    String getRaceString(boolean mod);
    
    String identify();
}
