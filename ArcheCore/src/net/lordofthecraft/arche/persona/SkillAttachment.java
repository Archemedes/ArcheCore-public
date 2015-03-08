package net.lordofthecraft.arche.persona;

import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.ArcheTask;
import net.lordofthecraft.arche.save.tasks.UpdateSkillTask;
import net.lordofthecraft.arche.skill.ArcheSkill;
import net.lordofthecraft.arche.skill.SkillData;

import org.bukkit.entity.Player;

public class SkillAttachment {
	private static final SaveHandler buffer = SaveHandler.getInstance();
	
	private FutureTask<SkillData> call;
	private double xp;
	private boolean canSee;
	
	private double modifier = -1;
	
	final ArcheSkill skill;
	
	private final String uuid;
	private final int id;
	private boolean error = false;
	
	SkillAttachment(ArcheSkill skill, ArchePersona persona, FutureTask<SkillData> call){
		this.call = call;
		
		xp = 0;
		canSee = skill.getVisibility() == Skill.VISIBILITY_VISIBLE;
		
		this.skill = skill;
		this.uuid = persona.getPlayerUUID().toString();
		this.id = persona.getId();
	}
	
	
	public void initialize(){
		if(call != null){
			SkillData data = null;
			
			try {
				data = call.get(200, TimeUnit.MILLISECONDS);
				//SkillData can be null, which means the table entry wasnt found
				//In this case player hasn't learned the skill yet, and we
				//use the initial values for SkillAttachment
				if(data != null){ 
					xp = data.xp;
					canSee = data.visible;
				}

				call = null;
			} catch(TimeoutException e){
				error = true;
				Logger log = ArcheCore.getPlugin().getLogger();
				log.severe("SQL interfacing thread is lagging behind.");
				log.severe("Skill data might be impossible to retrieve.");
				UUID u = UUID.fromString(uuid);
				ArchePersonaHandler.getInstance().unload(u);
				Player x = ArcheCore.getPlayer(u);
				if( x == null){ log.severe("ERROR: SkillAttachment owning Player " + uuid + "was not found online.");
				}else {
					log.severe("Kicking player " + x.getName() + "in an effort to preserve skill data integrity.");
					log.severe("Sorry, " + x.getName() + " :(");
					x.kickPlayer("Skill Data error. Please reconnect.");
				}
				//e.printStackTrace();
			} catch (Exception e){ e.printStackTrace();}
		}
	}
	
	public boolean isInitialized(){
		return call == null;
	}
	
	public double getModifier(){
		return modifier;
	}
	
	public void setModifier(double modifier){
		this.modifier = modifier;
	}
	
	public double getXp(){
		return xp;
	}
	
	public boolean isVisible(){
		return canSee;
	}
	
	public void reveal(){
		if(!canSee){
			canSee = true;
			performSQLUpdate();
		}
	}
	
	public void addXp(double added){
		xp += added;
		
		performSQLUpdate();
	}
	
	public void removeXP(double removed){
		xp -= removed;
		
		performSQLUpdate();
	}
	
	public void setXp(double xp){
		this.xp = xp;
		performSQLUpdate();
	}
	
	private void performSQLUpdate(){
		if(error) return;
		ArcheTask task = new UpdateSkillTask(skill, uuid, id, xp, canSee);
		buffer.put(task);
	}
	
}
