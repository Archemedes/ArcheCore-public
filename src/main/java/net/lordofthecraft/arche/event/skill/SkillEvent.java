package net.lordofthecraft.arche.event.skill;

import net.lordofthecraft.arche.event.persona.PersonaEvent;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;

public abstract class SkillEvent extends PersonaEvent {
	private final Skill skill;
	
	public SkillEvent(Persona persona, Skill skill){
		super(persona);
		this.skill = skill;
	}
	
	public Skill getSkill(){
		return skill;
	}
	
}
