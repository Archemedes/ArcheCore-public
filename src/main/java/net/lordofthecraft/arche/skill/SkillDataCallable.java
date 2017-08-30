package net.lordofthecraft.arche.skill;

import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.persona.ArchePersona;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Callable;

public class SkillDataCallable implements Callable<SkillData> {
	private final ArchePersona persona;
	private final String skill;
	private final SQLHandler handler;
	
	public SkillDataCallable(ArchePersona persona, String skill, SQLHandler handler){
		this.persona = persona;
		this.skill = skill;
		this.handler = handler;
	}
	
	@Override
	public SkillData call() throws SQLException {
		//TODO update this statement
		String query = "SELECT xp,visible FROM sk_" + skill + " WHERE player='" + persona.getPlayerUUID().toString() + "' AND id=" + persona.getId() +";"; 
		SkillData data;
		
		synchronized(handler){
			ResultSet res = handler.query(query);
			
			if(res.next()){
				double xp = res.getDouble(1);
				boolean visible = res.getBoolean(2);
				data = new SkillData(xp, visible);
			} else {
				data = null;
			}
			
			res.close();
			res.getStatement().close();
		}
		
		return data;
	}

}
