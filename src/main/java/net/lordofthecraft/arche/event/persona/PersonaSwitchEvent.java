package net.lordofthecraft.arche.event.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a player is trying to switch current Personas
 * The Persona provided by this object is the Persona the player tries to switch to.
 */
public class PersonaSwitchEvent extends PersonaEvent implements Cancellable {
private static final HandlerList handlers = new HandlerList();

	private Persona before;
	//Generic Cancellable implementation
	private boolean cancelled = false;

	public PersonaSwitchEvent(Persona persona) {
		super(persona);
		before = ArcheCore.getControls().getPersonaHandler().getPersona(persona.getPlayer());
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	/**
	 * @return The persona the player is switching from. Can return a null value.
	 */

	public Persona getOriginPersona(){ return before; }

	@Override public boolean isCancelled() { return cancelled ; }
	@Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled ; }
}
