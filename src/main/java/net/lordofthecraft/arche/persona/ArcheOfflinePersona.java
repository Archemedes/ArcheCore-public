package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.persona.PersonaRenameEvent;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.rows.persona.update.PersonaUpdateRow;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.sql.Timestamp;
import java.util.UUID;

public class ArcheOfflinePersona implements OfflinePersona {

    static final IConsumer consumer = ArcheCore.getControls().getConsumer();
    final PersonaKey personaKey;
    final Timestamp creation;
    Timestamp lastPlayed;
    boolean current = false;
    Race race;
    String gender;
    PersonaType type;
    PersonaInventory inv;
    volatile String name;
    Timestamp lastRenamed;

    public ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, boolean current, Race race, String name, PersonaType type) {
        this.personaKey = personaKey;
        this.creation = creation;
        this.current = current;
    }

    public ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation) {
        this.personaKey = personaKey;
        this.creation = creation;
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
        return current;
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
    public String getGender() {
        return gender;
    }

    @Override
    public void setGender(String gender) {
        this.gender = gender;
        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.GENDER, gender, false));
        //buffer.put(new UpdateTask(this, PersonaField.GENDER, gender));
    }

    @Override
    public PersonaType getPersonaType() {
        return type;
    }

    @Override
    public void setPersonaType(PersonaType type) {
        this.type = type;

        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.TYPE, type, false));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Race getRace() {
        return race;
    }

    @Override
    public void setName(String name) {
        PersonaRenameEvent event = new PersonaRenameEvent(this, name);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        this.name = name;
        lastRenamed = new Timestamp(System.currentTimeMillis());

        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.NAME, name, false));
        consumer.queueRow(new PersonaUpdateRow(this, PersonaField.STAT_RENAMED, lastRenamed, false));
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
