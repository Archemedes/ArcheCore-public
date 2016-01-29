package net.lordofthecraft.arche.save.tasks;

import java.util.concurrent.FutureTask;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.skill.SkillData;
import net.lordofthecraft.arche.skill.SkillDataCallable;

public class SelectSkillTask extends ArcheTask {

	private final FutureTask<SkillData> future;
	
	public SelectSkillTask(ArchePersona persona, Skill s){
		super();
		SkillDataCallable callable = new SkillDataCallable(persona, s.getName(), handle);
		future = new FutureTask<SkillData>(callable);
		
		if(ArcheCore.getPlugin().debugMode())
			ArcheCore.getPlugin().getLogger().info("[Debug] Now creating a SelectSkillTask for " + persona.getPlayerName() + " and skill " + s.getName());
	} 
	
	public FutureTask<SkillData> getFuture(){
		return future;
	}
	
	@Override
	public void run(){
		future.run();
	}
}
