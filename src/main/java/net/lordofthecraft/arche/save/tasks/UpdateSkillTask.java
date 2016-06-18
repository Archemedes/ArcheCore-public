package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.skill.ArcheSkill;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateSkillTask extends ArcheTask {

	private final PreparedStatement stat;
	private final String uuid;
	private final int id;
	private final double xp;
	private final boolean visible;
	
	public UpdateSkillTask(ArcheSkill skill, String uuid, int id, double xp, boolean visible){
		super();
		this.uuid = uuid;
		this.id = id;
		stat = skill.getUpdateStatement();
		
		this.xp = xp;
		this.visible = visible;
	}
	
	@Override
	public void run(){
		
		try{
			stat.setString(1, uuid); 
			stat.setInt(2, id);
			stat.setDouble(3, xp);
			stat.setBoolean(4, visible);
			
			synchronized(handle){
				stat.execute();
			}
		} catch (SQLException e ){
			e.printStackTrace();
		}
	}
	
}
