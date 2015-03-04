package net.lordofthecraft.arche.event;

import org.bukkit.event.*;
import net.lordofthecraft.arche.interfaces.*;

public class PersonaDeactivateEvent extends PersonaEvent
{
    private final Reason reason;
    private static final HandlerList handlers;
    
    public PersonaDeactivateEvent(final Persona persona, final Reason reason) {
        super(persona);
        this.reason = reason;
    }
    
    public Reason getReason() {
        return this.reason;
    }
    
    public HandlerList getHandlers() {
        return PersonaDeactivateEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PersonaDeactivateEvent.handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
    
    public enum Reason
    {
        LOGOUT, 
        SWITCH, 
        REMOVE;
    }
}
