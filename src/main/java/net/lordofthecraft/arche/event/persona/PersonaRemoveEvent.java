package net.lordofthecraft.arche.event.persona;

import net.lordofthecraft.arche.interfaces.OfflinePersona;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a Persona is removed, even if to make room for a new Persona that is being created.
 */
public class PersonaRemoveEvent extends OfflinePersonaEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final boolean makeRoom;
    private boolean cancelled = false;

    public PersonaRemoveEvent(OfflinePersona persona, boolean makeRoom) {
        super(persona);
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

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean b) {
		this.cancelled = b;
	}
}
