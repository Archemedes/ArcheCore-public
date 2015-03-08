package net.lordofthecraft.arche.event;

import net.lordofthecraft.arche.interfaces.Persona;

import org.bukkit.event.HandlerList;

public class PersonaDeactivateEvent extends PersonaEvent {
	private final Reason reason;
	
	public enum Reason { LOGOUT, SWITCH, REMOVE; }
	
	public PersonaDeactivateEvent(Persona persona, Reason reason) {
		super(persona);
		this.reason = reason;
	}
	
	public Reason getReason(){
		return reason;
	}

	//HandlerList Boilerplate
	private static final HandlerList handlers = new HandlerList();
	@Override public HandlerList getHandlers() { return handlers ; }
	public static HandlerList getHandlerList() { return handlers ; }
	
}


