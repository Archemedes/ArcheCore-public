package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.skill.ArcheSkill;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateSkillTask extends StatementTask {

	//private final PreparedStatement stat;
	private final String uuid;
	private final double xp;
	private final boolean visible;
	private final int slot;
	private final String skillId;
	
	public UpdateSkillTask(ArcheSkill skill, String uuid, double xp, boolean visible, int slot){
		super();
		this.uuid = uuid;
		//stat = skill.getUpdateStatement();
		this.skillId = skill.getName();
		this.xp = xp;
		this.visible = visible;
		this.slot = slot;
	}
	
	/*@Override
	public void run(){
		
		try{
			//stat.setString(1, uuid);
			//stat.setInt(2, id);
			//stat.setDouble(3, xp);
			//stat.setBoolean(4, visible);
			
			synchronized(handle){
				//stat.execute();
			}
		} catch (SQLException e ){
			e.printStackTrace();
		}
	}*/

	@Override
	protected void setValues() throws SQLException {
		stat.setInt(1, slot);
		stat.setDouble(2, xp);
		stat.setBoolean(3, visible);
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
	protected String getQuery() {
		return "UPDATE persona_skills SET skill_slot=? AND xp=? AND visible=? WHERE persona_id_fk=? AND skill_fk=?";
	}
}
