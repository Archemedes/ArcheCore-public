package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.save.tasks.ArcheTask;
import net.lordofthecraft.arche.skill.SkillData;
import net.lordofthecraft.arche.skill.SkillDataCallable;

import java.util.concurrent.FutureTask;

public class SelectSkillTask extends ArcheTask {

	private final FutureTask<SkillData> future;
	
	//TODO this needs to be rewritten to the new database structure
	public SelectSkillTask(ArchePersona persona, Skill s){
		super();
		SkillDataCallable callable = new SkillDataCallable(persona, s.getName(), handle);
		future = new FutureTask<>(callable);
		
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
