package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.persona.PersonaInventory;
import org.bukkit.inventory.Inventory;

import java.sql.Timestamp;
import java.util.UUID;

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
     * Set the underlying type of this persona
     *
     * @param type The type of persona it should be.
     */
    void setPersonaType(PersonaType type);

    /**
     * Get the human-readable Gender of this Persona
     *
     * @return The Persona's gender
     */
    String getGender();

    /**
     * Assign a persona's gender to the specified gender.
     *
     * @param gender The persona's new gender.
     */
    void setGender(String gender);

    /**
     * Retrieve the Roleplay name of this Persona
     *
     * @return The Persona's RP name.
     */
    String getName();

    /**
     * Set the new RP name of this Persona. This also updates the time at which this Persona was last renamed
     *
     * @param name The new RP name.
     */
    void setName(String name);

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
     * @return The EnderChest inventory for this persona
     */
    Inventory getEnderChest();

    /**
     * @return The PersonaInventory for this persona
     */
    PersonaInventory getPInv();

    /**
     * @return the inventory of this persona as an Inventory object
     */
    Inventory getInventory();

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
     * Load persona from SQL.
     *
     * @return The loaded persona
     */
    Persona loadPersona();
}
