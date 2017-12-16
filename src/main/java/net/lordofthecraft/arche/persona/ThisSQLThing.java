package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.rows.persona.InsertPersonaRow;
import net.lordofthecraft.arche.save.rows.persona.UpdatePersonaRow;
import net.lordofthecraft.arche.save.rows.player.InsertPlayerUUIDRow;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

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
                int ge = rs.getInt("gender");
                String gender = ge == 1 ? "Male" : ge == 2 ? "Other" : "Female";
				boolean current = rs.getBoolean("current");
				Timestamp creation = new Timestamp(rs.getLong("stat_creation"));
				
				ArchePersona p = new ArchePersona(persona_id, uuid, slot, name, race, birthdate, gender, creation, new Timestamp(0), PersonaType.NORMAL);
				p.current = current;
				Location l = new Location(Bukkit.getWorlds().get(0), 274.5, 94.0, -271.5);
                OfflinePlayer opl = Bukkit.getOfflinePlayer(uuid);
                if (opl != null && opl.getName() != null) {
                    c.queueRow(new InsertPlayerUUIDRow(uuid, opl.getName()));
                    c.queueRow(new InsertPersonaRow(p, l));
                    c.queueRow(new UpdatePersonaRow(p, PersonaField.STAT_CREATION, creation));
                    c.queueRow(new UpdatePersonaRow(p, PersonaField.RACE, rs.getString("rheader")));
                    c.queueRow(new UpdatePersonaRow(p, PersonaField.STAT_PLAYED, 0));
                    c.queueRow(new UpdatePersonaRow(p, PersonaField.STAT_PLAYTIME_PAST, rs.getInt("stat_playtime_past") + rs.getInt("stat_played")));
                } /*else {
                    ArcheCore.getPlugin().getLogger().severe("ERROR! OfflinePlayer for "+uuid+" returned null!!! WE WILL NOT BE MAKING PERSONAS FOR THEM!");
                }*/
			}
			
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
