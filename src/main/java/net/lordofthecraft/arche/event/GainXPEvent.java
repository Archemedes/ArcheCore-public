package net.lordofthecraft.arche.event;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class GainXPEvent extends SkillEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private double xp;
	//Generic Cancellable implementation
	private boolean cancelled = false;

	public GainXPEvent(Persona persona, Skill skill, double xp) {
		super(persona, skill);
		this.xp = xp;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}
	
	public double getAmountGained(){
		return xp;
	}
	
	public void setAmountGained(double xp){
		this.xp = xp;
	}

	@Override public boolean isCancelled() { return cancelled ; }
	@Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled ; }

}
