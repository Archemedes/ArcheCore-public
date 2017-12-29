package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.persona.PersonaRemoveEvent;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import net.lordofthecraft.arche.interfaces.PersonaTags;
import net.lordofthecraft.arche.save.rows.persona.DeletePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.WeakBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ArcheOfflinePersona implements OfflinePersona {
    protected static final IConsumer consumer = ArcheCore.getConsumerControls();
    protected static final ArchePersonaHandler handler = ArchePersonaHandler.getInstance();
	
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
    String name;
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
        return personaKey.getPersonaId();
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
        return ArcheCore.getPlugin().getPlayerNameFromUUID(getPlayerUUID());
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
    public int getAge() {
    	int currentYear = ArcheCore.getControls().getCalendar().getYear();
    	int age = currentYear - birthdate;
    	return age;
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
        PersonaStore store = handler.getPersonaStore();
        String select = store.personaSelect + " AND persona_id=?";
        
        try (Connection connection = ArcheCore.getSQLControls().getConnection();
             PreparedStatement statement = connection.prepareStatement(select)) {
        	statement.setString(1, getPlayerUUID().toString());
        	statement.setInt(2, getPersonaId());
        	ResultSet rs = statement.executeQuery(); //closed when PrepStat is closed
        	
            return store.buildPersona(rs, this);
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
    public void remove() {
		PersonaRemoveEvent event = new PersonaRemoveEvent(this, false);
		Bukkit.getPluginManager().callEvent(event);

		handler.getPersonaStore().removePersona(this);
        consumer.queueRow(new DeletePersonaRow(this));
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
        return personaKey.getPersonaId();
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
