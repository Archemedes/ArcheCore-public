package net.lordofthecraft.arche.event;

import net.lordofthecraft.arche.interfaces.Persona;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 *Event called when a Player creates a Persona
 */
public class PersonaCreateEvent extends PersonaEvent implements Cancellable{
	private final Persona replaces;
	private static final HandlerList handlers = new HandlerList();
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
		
	public PersonaCreateEvent(Persona persona, Persona replaces) {
		super(persona);
		this.replaces = replaces;
	}
	
	/**
	 * Get the Persona that is currently holding the id/slot for this particular player. May not exist.
	 * @return The Persona that is to be replaced
	 */
	public Persona getReplacedPersona(){
		return replaces;
	}

	//Generic Cancellable implementation
	private boolean cancelled = false;
	@Override public boolean isCancelled() { return cancelled ; }
	@Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled ; }
}
