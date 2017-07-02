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

		/*
	    skill_id 		INT UNSIGNED AUTO_INCREMENT,
    skill_fk 		VARCHAR(255) NOT NULL,
    persona_id_fk 	CHAR(36) NOT NULL,
    skill_selected 	BOOLEAN DEFAULT FALSE,
    skill_slot 		TINYINT(1) DEFAULT 3,
    xp 				DOUBLE(255,2) DEFAULT 0.00,
    visible 		BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (skill_id),
    FOREIGN KEY (skill_fk) REFERENCES skills (skill),
    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id),
    CONSTRAINT unique_skills UNIQUE (persona_id_fk, skill_fk)
	 */

	@Override
	public SkillData call() throws SQLException {
		String query = "SELECT xp,visible,skill_slot FROM persona_skills WHERE persona_id_fk='" + persona.getPersonaId().toString() + " AND skill_fk='" + skill + "'";
		SkillData data;
		
		synchronized(handler){
			ResultSet res = handler.query(query);
			
			if(res.next()){
				double xp = res.getDouble("xp");
				boolean visible = res.getBoolean("visible");
				int slot = res.getInt("skill_slot");
				data = new SkillData(xp, visible, slot);
			} else {
				data = null;
			}
			
			res.close();
			res.getStatement().close();
		}
		
		return data;
	}

}
