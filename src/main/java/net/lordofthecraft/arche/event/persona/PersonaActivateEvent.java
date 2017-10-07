package net.lordofthecraft.arche.event.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import org.bukkit.event.HandlerList;

public class PersonaActivateEvent extends PersonaEvent{
	//HandlerList Boilerplate
	private static final HandlerList handlers = new HandlerList();
	private final Reason reason;
	
	public PersonaActivateEvent(Persona persona, Reason reason) {
		super(persona);
		this.reason = reason;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Reason getReason(){
		return reason;
	}

	@Override public HandlerList getHandlers() { return handlers ; }

    public enum Reason {LOGIN, SWITCH}

    @Override
    public boolean isLoadedEvent() {
        return true;
    }
}
