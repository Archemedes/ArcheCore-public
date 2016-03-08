package net.lordofthecraft.arche.save.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.PersonaField;

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
			Connection c = handle.getSQL().getConnection();
			PreparedStatement stat = field.getStatement(c);
			
				 if(value instanceof String)  stat.setString(1, (String) value); 
			else if(value instanceof Boolean) stat.setBoolean(1, (Boolean) value);
			else if(value instanceof Integer) stat.setInt(1, (Integer) value);
			else if(value instanceof Long) 	  stat.setLong(1, (Long) value);
			else if(value == null)			  stat.setString(1, null);
			else 							  stat.setString(1, value.toString());
			
			stat.setString(2, persona.getPlayerUUID().toString());
			stat.setInt(3, persona.getId());
			
			stat.execute();
		}catch(SQLException e){e.printStackTrace();}
	}
	

}