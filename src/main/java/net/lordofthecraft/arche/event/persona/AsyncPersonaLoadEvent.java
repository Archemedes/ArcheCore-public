package net.lordofthecraft.arche.event.persona;

import java.sql.Connection;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.lordofthecraft.arche.interfaces.Persona;

/**
 * Simple Event that fires when ArcheCore has async built a Persona
 * Runs during {@link org.bukkit.event.player.AsyncPlayerPreLoginEvent} but gives access to Persona
 * Also runs when Persona is loaded through command.
 */
public class AsyncPersonaLoadEvent extends Event {
	private final Persona persona;
	private final Connection c;
	
	//HandlerList Boilerplate
	private static final HandlerList handlers = new HandlerList();
	
	public AsyncPersonaLoadEvent(Persona persona, Connection c) {
		super(true);
		this.persona = persona;
		this.c = c;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public Persona getPersona() {
		return persona;
	}
	
	/**
	 * Save yourself the trouble of opening a connection. Don't close it unless you're a dick.
	 * @return Connection that was used to built the Persona.
	 */
	public Connection getSQLConnection() {
		return c;
	}

	@Override public HandlerList getHandlers() { return handlers ; }
}
