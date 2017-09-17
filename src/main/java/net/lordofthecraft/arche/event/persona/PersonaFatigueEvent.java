package net.lordofthecraft.arche.event.persona;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.lordofthecraft.arche.interfaces.Persona;

/**
 * Event is called whenever the Persona's fatigue is modified
 * called in the ArchePersona setFatigue method.
 * @author Sporadic
 *
 */
public class PersonaFatigueEvent extends PersonaEvent implements Cancellable {
	private double modifier;
	
	public PersonaFatigueEvent(Persona p, double fatigue){
		super(p);
		this.modifier = fatigue;
	}
	
	/**
	 * Get the value the fatigue of the Persona will be set to
	 * @return The new fatigue value
	 */
	public double getNewFatigue() {
		return modifier;
	}
	
	/**
	 * Get the value the fatigue of the Persona will be set to
	 * @param amount the new fatigue value
	 */
	public void setNewFatigue(double amount) {
		modifier = amount;
	}
	
	//We could possibly also add a reason for fatigue here but currently not needed.
	
	//Double boilerplate implementation
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	
	@Override public boolean isCancelled() { return cancelled ; }
	@Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled ; }
	public static HandlerList getHandlerList() {return handlers;}
	public HandlerList getHandlers() {return handlers;}
}
