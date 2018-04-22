package net.lordofthecraft.arche.event.persona;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Event that is sent when a whois call is being made on a Persona
 * @author Sporadic
 */
public class PersonaWhoisEvent extends OfflinePersonaEvent implements Cancellable {
	final private List<BaseComponent> whatIsSend;
	final private CommandSender whosAsking;
	final private Query query;
	final private boolean mod;
	final private boolean disguised;

    public PersonaWhoisEvent(OfflinePersona p, CommandSender who, List<BaseComponent> sent, Query query, boolean mod, boolean disguised) {
    	super(p);
    	whosAsking = who;
    	this.query = query;
    	this.whatIsSend = sent;
    	this.mod = mod;
    	this.disguised = disguised;
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
	
	public boolean isDisguised() {
		return disguised;
	}
	
	public CommandSender getWhosAsking() {
		return whosAsking;
	}
	
	/**
	 * Query determines if basic or extended info is being sent, 
	 * or if extended info is only being investigated but not sent
	 * @return the query type
	 */
	public Query getQuery() {
		return query;
	}

    public enum Query {
        BEACON_ICON, BASIC, EXTENDED_PROBE, EXTENDED
    }

    //Double boilerplate implementation
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	
	@Override public boolean isCancelled() { return cancelled ; }
	@Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled ; }
	public static HandlerList getHandlerList() {return handlers;}
	public HandlerList getHandlers() {return handlers;}
}
