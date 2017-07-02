package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.PersonaInventory;
import org.bukkit.Location;

import java.sql.SQLException;
import java.util.UUID;

public class PersonaSwitchTask extends StatementTask {
	private final UUID id;
	private final Location l;
	private final PersonaInventory inv;
	
	public PersonaSwitchTask(ArchePersona p){
		this.id = p.getPersonaId();
		this.l = p.getLocation();
		this.inv = p.getPInv();
	}
	
	@Override
	protected void setValues() throws SQLException {
		stat.setString(1, l.getWorld().getUID().toString());
		stat.setInt(2, l.getBlockX());
		stat.setInt(3, l.getBlockY());
		stat.setInt(4, l.getBlockZ());
		stat.setString(5, inv.getAsString());
		stat.setString(6, id.toString());
	}

	@Override
	protected String getQuery(){return "UPDATE persona_world SET world=?, x=?, y=?, z=?, inv=? WHERE persona_id_fk=?";}

}
