package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.tasks.ArcheTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateTask extends ArcheTask {
	private final Persona persona;
	private final PersonaField field;
	private final Object value;
	
	public UpdateTask(Persona persona, PersonaField field, Object value){
		this.persona = persona;
		this.field = field;
		this.value = value;
	}
	
	@Override
	public void run(){
		try{
			Connection c = handle.getConnection();
			PreparedStatement stat = field.getStatement(c);
			stat.setObject(1, value, field.type);
			stat.setInt(2, persona.getPersonaId());
			stat.execute();
		}catch(SQLException e){e.printStackTrace();}
	}
	

}
