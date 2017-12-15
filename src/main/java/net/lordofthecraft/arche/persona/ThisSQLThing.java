package net.lordofthecraft.arche.persona;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.rows.persona.InsertPersonaRow;
import net.lordofthecraft.arche.save.rows.persona.UpdatePersonaRow;

public class ThisSQLThing {
	
	public static void go(){
		SQLHandler o = new ArcheSQLiteHandler(ArcheCore.getPlugin(), "ArcheCore_old", 2000000);

		int id = 502;
		IConsumer c = ArcheCore.getConsumerControls();
		try {
			ResultSet rs = o.query("SELECT * FROM persona");
			while( rs.next() ) {
				int persona_id = id++;
				if(id % 1000 == 0) System.out.println("Issuing id: " + id);
				
				UUID uuid = UUID.fromString(rs.getString("player"));
				int slot = rs.getInt("id");
				int birthdate = ArcheCore.getControls().getCalendar().getYear() - rs.getInt("age");
				Race race = Race.valueOf(rs.getString("race"));
				String name = rs.getString("name");
				String gender = rs.getInt("gender") == 1? "Male" : "Female";
				boolean current = rs.getBoolean("current");
				Timestamp creation = new Timestamp(rs.getLong("stat_creation"));
				
				ArchePersona p = new ArchePersona(persona_id, uuid, slot, name, race, birthdate, gender, creation, new Timestamp(0), PersonaType.NORMAL);
				p.current = current;
				Location l = new Location(Bukkit.getWorlds().get(0), 274.5, 94.0, -271.5);
				
				c.queueRow(new InsertPersonaRow(p, l));
				c.queueRow(new UpdatePersonaRow(p, PersonaField.STAT_CREATION, creation));
				c.queueRow(new UpdatePersonaRow(p, PersonaField.RACE, rs.getString("rheader")));
				c.queueRow(new UpdatePersonaRow(p, PersonaField.STAT_PLAYED, rs.getInt("stat_played")));
				c.queueRow(new UpdatePersonaRow(p, PersonaField.STAT_PLAYTIME_PAST, rs.getInt("stat_playtime_past")));
			}
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
