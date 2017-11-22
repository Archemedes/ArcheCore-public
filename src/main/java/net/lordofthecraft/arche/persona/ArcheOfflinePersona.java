package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import net.lordofthecraft.arche.interfaces.PersonaTags;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.WeakBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ArcheOfflinePersona implements OfflinePersona {
    final ArchePersonaTags tags = new ArchePersonaTags(this);

    boolean deleted = false;
    final AtomicInteger timePlayed;
    final PersonaKey personaKey;
    final Timestamp creation;
    Timestamp lastPlayed;
    boolean current = false;
    Race race;
    int birthdate;
    String gender;
    protected PersonaType type;
    WeakBlock location;
    volatile String name;
    Timestamp lastRenamed;


    ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, boolean current, 
    		Race race, int birthdate, String gender, PersonaType type, String name) {
        this.personaKey = personaKey;
        this.creation = creation;
        this.current = current;
        this.race = race;
        this.birthdate = birthdate;
        this.gender = gender;
        this.type = type;
        this.name = name;
        timePlayed = new AtomicInteger(0);
    }

    ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, Timestamp lastPlayed, boolean current, 
    		Race race, int birthdate, String gender, PersonaType type, String name) {
        this.personaKey = personaKey;
        this.creation = creation;
        this.lastPlayed = lastPlayed;
        this.current = current;
        this.race = race;
        this.birthdate = birthdate;
        this.gender = gender;
        this.type = type;
        this.name = name;
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
        return Bukkit.getOfflinePlayer(personaKey.getPlayerUUID()).getName();
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
    public Timestamp getCreationTime() {
        return creation;
    }

    @Override
    public int getDateOfBirth() {
    	return birthdate;
    }
    
    @Override
    public String getGender() {
        return gender;
    }

    @Override
    public boolean isMale() {
        return "Male".equals(gender);
    }

    @Override
    public boolean isFemale() {
        return "Female".equals(gender);
    }

    @Override
    public PersonaType getPersonaType() {
        return type;
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
    public boolean isLoaded() {
        return this instanceof ArchePersona;
    }

    @Override
    public Persona loadPersona() {
        PersonaStore store = ArchePersonaHandler.getInstance().getPersonaStore();
        String select = store.personaSelect + " AND persona_id=" + personaKey.getPersonaID();

        try (Connection connection = ArcheCore.getSQLControls().getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(select)) {
            return store.buildPersona(rs, this.getPlayer());
        } catch (SQLException e) {
            throw new RuntimeException(e); //zero fucks given
        }
    }

    @Override
    public ArchePersona getPersona() {
        if (!isLoaded()) {
            return null;
        }
        return (ArchePersona) this;
    }

    @Override
    public boolean remove() {
        return false;
    }

    @Override
    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public PersonaTags tags() {
        return tags;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(personaKey.getPlayerUUID());
    }

    @Override
    public int getTimePlayed() {
        return timePlayed.get();
    }

    @Override
    public int hashCode() {
        return personaKey.getPersonaID();
    }

    public Location getLocation() {
        if (location == null) return null;
        else return location.toLocation();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) return false;
        //Note that due to demanding exact class equality (comrade)
        //this equals also works for ArchePersona equals ArchePersona
        //but not equal for ArchePersona and ArcheOfflinePersona 
        if (object.getClass() != this.getClass()) return false;
        ArcheOfflinePersona p = (ArcheOfflinePersona) object;
        return this.getPersonaId() == p.getPersonaId();
    }

    @Override
    public String toString() { //Also works for ArchePersona
        return this.getClass().getSimpleName() + ": " + MessageUtil.identifyPersona(this);
    }
}
