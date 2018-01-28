package net.lordofthecraft.arche.event.persona;

import org.bukkit.event.HandlerList;

import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;

/**
 *Event called when a Player creates a Persona
 */
public class PersonaCreateEvent extends PersonaEvent {
    private static final HandlerList handlers = new HandlerList();
    private final OfflinePersona replaces;

	public PersonaCreateEvent(Persona persona, OfflinePersona replaces) {
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
	public OfflinePersona getReplacedPersona(){
		return replaces;
	}
}
