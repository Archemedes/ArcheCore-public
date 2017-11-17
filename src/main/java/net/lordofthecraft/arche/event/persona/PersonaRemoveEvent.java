package net.lordofthecraft.arche.event.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import org.bukkit.event.HandlerList;

/**
 * Called when a Persona is removed, even if to make room for a new Persona that is being created.
 * Cancelling this event will also cancel Persona creation if two Personas compete for the same id.
 */
public class PersonaRemoveEvent extends PersonaDeactivateEvent {
    private static final HandlerList handlers = new HandlerList();
    private final boolean makeRoom;

    public PersonaRemoveEvent(Persona persona, boolean makeRoom) {
        super(persona, Reason.REMOVE);
		this.makeRoom = makeRoom;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}
	
	/**
	 * Sometimes Personas are removed because the Player has created a new Persona
	 * with the same slot. If this is true, the boolean returned by this method returns true.
	 * @return If the Persona is removed to make room for a new Persona
	 */
	public boolean isToMakeRoom(){
		return makeRoom;
	}

}
