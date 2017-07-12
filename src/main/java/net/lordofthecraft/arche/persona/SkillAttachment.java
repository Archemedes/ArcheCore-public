package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.ProfessionSlot;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.save.SaveExecutorManager;
import net.lordofthecraft.arche.save.tasks.ArcheTask;
import net.lordofthecraft.arche.save.tasks.skills.UpdateSkillTask;
import net.lordofthecraft.arche.skill.ArcheSkill;
import net.lordofthecraft.arche.skill.SkillData;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class SkillAttachment {
	private static final SaveExecutorManager buffer = SaveExecutorManager.getInstance();
	final ArcheSkill skill;
	private final String uuid;
	private FutureTask<SkillData> call;
	private double xp;
	private boolean canSee;
	private double modifier = -1;
	private boolean error = false;
	private ProfessionSlot slot;
	private int level = 0;
	
	SkillAttachment(ArcheSkill skill, ArchePersona persona, FutureTask<SkillData> call){
		this.call = call;
		
		xp = 0;
		canSee = skill.getVisibility() == Skill.VISIBILITY_VISIBLE;
		
		this.skill = skill;
		this.uuid = persona.getPersonaId().toString();
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
					slot = ProfessionSlot.byInt(data.slot);
					level = ArcheSkill.getLevelFromXp(xp);
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

	public int getLevel() {
		return level;
	}

	public ProfessionSlot getSlot() {
		return slot;
	}

	public void setXp(double xp) {
		this.xp = xp;
		performSQLUpdate();
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
		if (xp > ArcheSkill.getXpFromLevel(level+1) && level < 1000) {
			++level;
		}

		performSQLUpdate();
	}
	
	public void removeXP(double removed){
		xp -= removed;
		if (xp < ArcheSkill.getXpFromLevel(level) && level > 0) {
			--level;
		}

		performSQLUpdate();
	}

	public void setSlot(ProfessionSlot slot) {
		this.slot = slot;

		performSQLUpdate();
	}
	private void performSQLUpdate(){
		if(error) return;
		ArcheTask task = new UpdateSkillTask(skill, uuid, xp, canSee, slot.getSlot());
		buffer.submit(task);
	}

	public ArcheSkill getSkill() {
		return skill;
	}
}
