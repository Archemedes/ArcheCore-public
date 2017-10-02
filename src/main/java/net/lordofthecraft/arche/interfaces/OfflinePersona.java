package net.lordofthecraft.arche.interfaces;

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

    boolean isLoaded();

    Persona getPersona();
}
