package net.lordofthecraft.arche.save.tasks;

import java.sql.SQLException;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;

public class PersonaRenameTask extends StatementTask {
	private final PersonaKey key;
	private final String name;
	
	public PersonaRenameTask(Persona pers){
		key = pers.getPersonaKey();
		name = pers.getName();
	}
	
	@Override
	protected void setValues() throws SQLException {
		stat.setString(1, key.getPlayerUUID().toString());
		stat.setInt(2, key.getPersonaId());
		stat.setString(3, name);
	}

	@Override
	protected String getQuery() {
		return "INSERT INTO persona_names (player,id,name) VALUES (?,?,?)";
	}

}
