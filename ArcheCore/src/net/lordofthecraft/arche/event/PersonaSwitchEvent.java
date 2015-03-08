package net.lordofthecraft.arche.event;

import net.lordofthecraft.arche.interfaces.Persona;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a player is trying to switch current Personas
 * The Persona provided by this object is the Persona the player tries to switch to.
 */
public class PersonaSwitchEvent extends PersonaEvent implements Cancellable {
private static final HandlerList handlers = new HandlerList();
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public PersonaSwitchEvent(Persona persona) {
		super(persona);
	}

	//Generic Cancellable implementation
	private boolean cancelled = false;
	@Override public boolean isCancelled() { return cancelled ; }
	@Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled ; }
}
