package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

public class PersonaRenameTask extends StatementTask {
    private final int persona_id;
    private final String name;
	
	public PersonaRenameTask(Persona pers){
        persona_id = pers.getPersonaId();
        name = pers.getName();
	}
	
	@Override
	protected void setValues() throws SQLException {
        stat.setInt(1, persona_id);
        stat.setString(2, name);
    }

	@Override
	protected String getQuery() {
        return "INSERT INTO persona_names (persona_id_fk,name) VALUES (?,?)";
    }

}
