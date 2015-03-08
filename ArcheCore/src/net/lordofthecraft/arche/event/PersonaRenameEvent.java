package net.lordofthecraft.arche.event;

import net.lordofthecraft.arche.interfaces.Persona;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class PersonaRenameEvent extends PersonaEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private String newName;
	
	public PersonaRenameEvent(Persona persona, String newName) {
		super(persona);
		this.newName = newName;
	}
	
	public String getNewName(){
		return newName;
	}
	
	public void setNewName(String newName){
		this.newName = newName; 
	}
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	//Generic Cancellable implementation
	private boolean cancelled = false;
	@Override public boolean isCancelled() { return cancelled ; }
	@Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled ; }

}
