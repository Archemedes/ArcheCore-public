package net.lordofthecraft.arche.event.persona;

import java.util.List;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.lordofthecraft.arche.interfaces.Persona;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Event that is sent when a whois call is being made on a Persona
 * @author Sporadic
 */
public class PersonaWhoisEvent extends PersonaEvent implements Cancellable {
	final private List<BaseComponent> whatIsSend;
	final private Query query;
	final private boolean mod;
	
	public PersonaWhoisEvent(Persona p, List<BaseComponent> sent, Query query, boolean mod) {
		super(p);
		this.query = query;
		this.whatIsSend = sent;
		this.mod = mod;
	}
	
	/**
	 * List of all the persona tags from the whois. List is member of the event class
	 * and changes made to it will be reflected into the info sent to player
	 * @return List of BaseComponents being sent
	 */
	public List<BaseComponent> getSent(){
		return whatIsSend;
	}
	
	/**
	 * Presumably this checks if the whois info was requested by a mod for this persona
	 * @return whether or not mod=true in whois
	 */
	public boolean isForMod() {
		return mod;
	}
	
	/**
	 * Query determines if basic or extended info is being sent, 
	 * or if extended info is only being investigated but not sent
	 * @return the query type
	 */
	public Query getQuery() {
		return query;
	}
	
	public static enum Query{
		BASIC, EXTENDED_PROBE, EXTENDED;
	}
	
	//Double boilerplate implementation
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	
	@Override public boolean isCancelled() { return cancelled ; }
	@Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled ; }
	public static HandlerList getHandlerList() {return handlers;}
	public HandlerList getHandlers() {return handlers;}
}
