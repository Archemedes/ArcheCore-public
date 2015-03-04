package net.lordofthecraft.arche.event;

import net.lordofthecraft.arche.interfaces.*;

public abstract class SkillEvent extends PersonaEvent
{
    private final Skill skill;
    
    public SkillEvent(final Persona persona, final Skill skill) {
        super(persona);
        this.skill = skill;
    }
    
    public Skill getSkill() {
        return this.skill;
    }
}
