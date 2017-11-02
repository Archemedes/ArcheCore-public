package net.lordofthecraft.arche.persona;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.persona.PersonaRenameEvent;
import net.lordofthecraft.arche.interfaces.Creature;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.rows.persona.UpdatePersonaRow;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.WeakBlock;

public class ArcheOfflinePersona implements OfflinePersona, InventoryHolder {

    static final IConsumer consumer = ArcheCore.getControls().getConsumer();
    boolean deleted = false;
    final AtomicInteger timePlayed;
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
    int pastPlayTime; //stat_playtime_past


    ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, boolean current, Race race, String gender, PersonaType type, String name) {
        this.personaKey = personaKey;
        this.creation = creation;
        this.current = current;
        this.race = race;
        this.gender = gender;
        this.type = type;
        this.name = name;
        timePlayed = new AtomicInteger(0);
    }

    ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, Timestamp lastPlayed, boolean current, Race race, String gender, PersonaType type, String name) {
        this.personaKey = personaKey;
        this.creation = creation;
        this.lastPlayed = lastPlayed;
        this.current = current;
        this.race = race;
        this.gender = gender;
        this.type = type;
        this.name = name;
        timePlayed = new AtomicInteger(0);
    }

    ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation) {
        this.personaKey = personaKey;
        this.creation = creation;
        timePlayed = new AtomicInteger(0);
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
        //TODO return player name
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
    public Inventory getInventory() {
        return inv.getInventory();
    }

    @Override
    public Inventory getEnderChest() {
        return inv.getEnderInventory();
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
        consumer.queueRow(new UpdatePersonaRow(this, PersonaField.GENDER, gender));
        //buffer.put(new UpdateTask(this, PersonaField.GENDER, gender));
    }

    @Override
    public PersonaType getPersonaType() {
        return type;
    }

    @Override
    public void setPersonaType(PersonaType type) {
        this.type = type;

        consumer.queueRow(new UpdatePersonaRow(this, PersonaField.TYPE, type));
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

        consumer.queueRow(new UpdatePersonaRow(this, PersonaField.NAME, name));
        consumer.queueRow(new UpdatePersonaRow(this, PersonaField.STAT_RENAMED, lastRenamed));
    }


    @Override
    public boolean isLoaded() {
        return this instanceof Persona;
    }

    @Override
    public Persona loadPersona(ResultSet res) throws SQLException {
        ArchePersona persona = new ArchePersona(this);
        persona.description = res.getString(PersonaField.DESCRIPTION.field());
        persona.prefix = res.getString(PersonaField.PREFIX.field());
        persona.current = res.getBoolean(PersonaField.CURRENT.field());
        persona.fatigue = res.getInt(PersonaField.FATIGUE.field());
        persona.health = res.getDouble(PersonaField.HEALTH.field());
        persona.food = res.getInt(PersonaField.FOOD.field());
        persona.saturation = res.getFloat(PersonaField.SATURATION.field());

        persona.timePlayed.set(res.getInt(PersonaField.STAT_PLAYED.field()));
        persona.charactersSpoken.set(res.getInt(PersonaField.STAT_CHARS.field()));
        persona.lastRenamed = res.getTimestamp(PersonaField.STAT_RENAMED.field());
        //persona.gainsXP = res.getBoolean(15);
        persona.skills.setMainProfession(ArcheSkillFactory.getSkill(res.getString(PersonaField.SKILL_SELECTED.field())));
        Optional<Creature> creature = ArcheCore.getMagicControls().getCreatureById(res.getString("creature"));
        creature.ifPresent(persona.magics::setCreature);

        String wstr = res.getString(PersonaField.WORLD.field());
        if (!res.wasNull()) {
            UUID wuuid = UUID.fromString(wstr);
            World w = Bukkit.getWorld(wuuid);
            if (w != null) {
                int x = res.getInt(PersonaField.X.field());
                int y = res.getInt(PersonaField.Y.field());
                int z = res.getInt(PersonaField.Z.field());
                persona.location = new WeakBlock(w, x, y, z);
            }
        }
        persona.loadPotionsFromString(res.getString(PersonaField.POTIONS.field()));

        if (ArcheCore.getControls().usesEconomy()) persona.money = res.getDouble(PersonaField.MONEY.field());
        persona.pastPlayTime = res.getInt(PersonaField.STAT_PLAYTIME_PAST.field());

        Connection connection = ArcheCore.getSQLControls().getConnection();

        persona.loadMagics(connection);

        persona.loadTags(connection);

        persona.loadAttributes(connection);

        persona.loadSkills(connection);
        
        persona.loadSkin(connection);

        connection.close();
        return persona;
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

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(personaKey.getPlayerUUID());
    }

    @Override
    public int getTotalPlaytime() {
        return pastPlayTime + getTimePlayed();
    }

    @Override
    public int getTimePlayed() {
        return timePlayed.get();
    }
    
    @Override
    public int hashCode() {
        return personaKey.getPersonaID();
    }
    
    @Override
    public boolean equals(Object object) {
        if(object == null) return false;
        //Note that due to demanding exact class equality (comrade)
        //this equals also works for ArchePersona equals ArchePersona
        //but not equal for ArchePersona and ArcheOfflinePersona 
		if(object.getClass() != this.getClass()) return false;
		ArcheOfflinePersona p = (ArcheOfflinePersona) object;
        return this.getPersonaId() == p.getPersonaId();
    }
    
    @Override
    public String toString() { //Also works for ArchePersona
    	return this.getClass().getSimpleName() + ": " + MessageUtil.identifyPersona(this);
    }
}
