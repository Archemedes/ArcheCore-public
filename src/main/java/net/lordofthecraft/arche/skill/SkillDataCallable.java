package net.lordofthecraft.arche.skill;

import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.persona.ArchePersona;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
        String query = "SELECT xp,visible FROM persona_skills WHERE persona_id_fk=? AND skill_id_fk=?";
        SkillData data;
		
		synchronized(handler){
            Connection conn = handler.getConnection();
            if (handler instanceof WhySQLHandler) {
                conn.setReadOnly(true);
            }
            PreparedStatement stat = conn.prepareStatement(query);
            stat.setString(1, persona.getPersonaId().toString());
            stat.setString(2, skill);
            ResultSet res = stat.executeQuery();

            if(res.next()){
                double xp = res.getDouble("xp");
                boolean visible = res.getBoolean("visible");
                data = new SkillData(xp, visible, 0);
			} else {
				data = null;
			}
			
			res.close();
			res.getStatement().close();
            if (handler instanceof WhySQLHandler) {
                conn.close();
            }
        }
		
		return data;
	}

}
