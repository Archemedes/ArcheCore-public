package net.lordofthecraft.arche.interfaces;

import java.sql.Timestamp;
import java.util.UUID;

import org.bukkit.OfflinePlayer;

import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.save.rows.ArchePersonaRow;

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
     * Get the loaded persona object, if applicable.
     * @see #isLoaded()
     * @return The loaded persona.
     */
    Persona getPersona();

    /**
     * Delete the Persona from the Plugin records.
     *
     * @return whether or not the removal was successful.
     */
    boolean remove();

    /**
     * Load persona from SQL. This makes no attempt to register the Persona inside PersonaHandler
     * as a result there is no guarantee any save-to-db methods on Persona will be updated in time 
     * if the Persona is requested for loading by another resource or by ArcheCore itself.
     * @return The loaded persona
     */
    Persona loadPersona();

    /**
     * Check whether or not this Persona has had it's SQL removed.
     * <p>
     * <b>Why?</b> When performing tasks such as {@link ArchePersonaRow} there is the chance that
     * a persona might already be removed. In the efforts to ensure that we are not unneedingly running tasks on personas which have no longer exist
     * this variable is set.
     * <p>
     * P.S. Don't set this value manually. Thanks.
     *
     * @return If this persona was successfully deleted
     */
    boolean isDeleted();

    /**
     * Retrieve the total Playtime this Persona has seen since its creation.
     *
     * @return Playtime in minutes
     */
    int getTimePlayed();
    
    /**
     * Persona tags are additional data that is automatically made persistent by ArcheCore's SQL backend
     * Dependent plugins can set and check tags for each Persona. Tags can also be modified by command.
     * Persona must be loaded (i.e. instanceof ArchePersona) for modifying methods to be successful.
     * @return The Persona-bound tag handler.
     */
    PersonaTags tags();

    OfflinePlayer getPlayer();
}
