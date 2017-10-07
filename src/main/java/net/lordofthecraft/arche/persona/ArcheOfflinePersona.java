package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import org.bukkit.inventory.Inventory;

import java.sql.Timestamp;
import java.util.UUID;

public class ArcheOfflinePersona implements OfflinePersona {

    private final PersonaKey personaKey;
    private final Timestamp creation;
    private boolean isCurrent = false;
    private PersonaInventory inventory;

    public ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, boolean isCurrent, PersonaInventory inventory) {
        this.personaKey = personaKey;
        this.creation = creation;
        this.isCurrent = isCurrent;
        this.inventory = inventory;
    }

    public ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, PersonaInventory inventory) {
        this.personaKey = personaKey;
        this.creation = creation;
        this.inventory = inventory;
    }

    @Override
    public int getPersonaId() {
        return personaKey.getPersonaID();
    }

    @Override
    public int getSlot() {
        return personaKey.getPersonaSlot();
    }

    @Override
    public boolean isCurrent() {
        return isCurrent;
    }

    @Override
    public String getPlayerName() {
        return null;
    }

    @Override
    public PersonaKey getPersonaKey() {
        return personaKey;
    }

    @Override
    public UUID getPlayerUUID() {
        return personaKey.getPlayerUUID();
    }

    @Override
    public Inventory getEnderChest() {
        return null;
    }

    @Override
    public PersonaInventory getPInv() {
        return null;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    @Override
    public Timestamp getCreationTime() {
        return creation;
    }

    @Override
    public boolean isLoaded() {
        return this instanceof Persona;
    }

    @Override
    public Persona loadPersona() {
        //TODO Load.
        return null;
    }

    @Override
    public Persona getPersona() {
        if (!isLoaded()) {
            return null;
        }
        return (Persona) this;
    }

    @Override
    public boolean remove() {
        return false;
    }
}
