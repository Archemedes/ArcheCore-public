package net.lordofthecraft.arche.event.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import org.bukkit.event.HandlerList;

/**
 *Event called when a Player creates a Persona
 */
public class PersonaCreateEvent extends PersonaEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Persona replaces;
	//Generic Cancellable implementation
	private boolean cancelled = false;

	public PersonaCreateEvent(Persona persona, Persona replaces) {
		super(persona);
		this.replaces = replaces;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * Get the Persona that is currently holding the id/slot for this particular player. May not exist.
	 * @return The Persona that is to be replaced
	 */
	public Persona getReplacedPersona(){
		return replaces;
	}
}
