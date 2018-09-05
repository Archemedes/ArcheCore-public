package net.lordofthecraft.arche.persona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import lombok.AccessLevel;
import lombok.Getter;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.persona.PersonaRemoveEvent;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import net.lordofthecraft.arche.interfaces.PersonaTags;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.rows.persona.DeletePersonaRow;
import net.lordofthecraft.arche.save.rows.persona.UpdatePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.WeakBlock;

@Getter
public class ArcheOfflinePersona implements OfflinePersona {
	protected static final IConsumer consumer = ArcheCore.getConsumerControls();
	protected static final ArchePersonaHandler handler = ArchePersonaHandler.getInstance();

	@Getter(AccessLevel.NONE) final ArchePersonaTags tags = new ArchePersonaTags(this);

	final PersonaKey personaKey;
	final Timestamp creationTime;
	boolean current = false;
	Race race;
	int dateOfBirth;
	String gender;
	PersonaType personaType;
	String name;
	String raceString = null;

	WeakBlock location;
	Timestamp lastPlayed;
	final AtomicInteger timePlayed;

	ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, int played,
			boolean current, Race race, int birthdate, String gender, PersonaType type, String name) {
		this(personaKey, creation, played, current, race, birthdate, gender, type, name, null);
	}

	ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, int played,
			boolean current, Race race, int birthdate, String gender, PersonaType type, String name, String raceString) {
		this.personaKey = personaKey;
		this.creationTime = creation;
		this.current = current;
		this.race = race;
		this.dateOfBirth = birthdate;
		this.gender = gender;
		this.personaType = type;
		this.name = name;
		timePlayed = new AtomicInteger(played);
	}

	ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, Timestamp lastPlayed, int played,
			boolean current, Race race, int birthdate, String gender, PersonaType type, String name) {
		this(personaKey, creation, lastPlayed, played, current, race, birthdate, gender, type, name, null);
	}

	ArcheOfflinePersona(PersonaKey personaKey, Timestamp creation, Timestamp lastPlayed, int played,
			boolean current, Race race, int birthdate, String gender, PersonaType type, String name, String raceString) {
		this.personaKey = personaKey;
		this.creationTime = creation;
		this.lastPlayed = lastPlayed;
		this.current = current;
		this.race = race;
		this.dateOfBirth = birthdate;
		this.gender = gender;
		this.personaType = type;
		this.name = name;
		timePlayed = new AtomicInteger(played);
		this.raceString = raceString;
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
	public String getPlayerName() {
		return ArcheCore.getPlugin().getPlayerNameFromUUID(getPlayerUUID());
	}

	@Override
	public UUID getPlayerUUID() {
		return personaKey.getPlayerUUID();
	}

	@Override
	public int getTimePlayed() {
		return timePlayed.get();
	}

	@Override
	public int getAge() {
		int currentYear = ArcheCore.getControls().getCalendar().getYear();
		int age = currentYear - dateOfBirth;
		return age;
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
	public PersonaTags tags() {
		return tags;
	}

	@Override
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(personaKey.getPlayerUUID());
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

	@Override
	public String getRaceString(boolean mod) {
		StringBuilder sb = new StringBuilder();
		if (raceString != null && !raceString.isEmpty()) {
			if (mod) {
				sb.append(raceString).append(ChatColor.GRAY).append(" (").append(race.getName()).append(")");
			} else {
				sb.append(raceString);
			}
		} else {
			if (race != Race.UNSET) {
				sb.append(race.getName());
			} else if (mod) {
				sb.append(ChatColor.GRAY).append(race.getName());
			}
		}
		return sb.toString();
	}

	@Override
	public void setApparentRace(String race){
		raceString = race;

		consumer.queueRow(new UpdatePersonaRow(this, PersonaField.RACE, race));
	}
}
