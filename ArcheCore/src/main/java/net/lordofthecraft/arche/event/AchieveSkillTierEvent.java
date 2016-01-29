package net.lordofthecraft.arche.event;

import net.lordofthecraft.arche.enums.SkillTier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;

import org.bukkit.event.HandlerList;

public class AchieveSkillTierEvent extends SkillEvent {
	private final SkillTier old, now;
	
	public AchieveSkillTierEvent(Persona persona, Skill skill, SkillTier old, SkillTier now) {
		super(persona, skill);
		this.old = old;
		this.now = now;
	}
	
	public SkillTier getFormerTier(){
		return old;
	}

	public SkillTier getAchievedTier(){
		return now;
	}
	
	//HandlerList Boilerplate
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers ; }
	public static HandlerList getHandlerList() { return handlers ; }

}
