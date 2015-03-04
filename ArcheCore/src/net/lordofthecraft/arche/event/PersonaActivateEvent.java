package net.lordofthecraft.arche.event;

import org.bukkit.event.*;
import net.lordofthecraft.arche.interfaces.*;

public class PersonaActivateEvent extends PersonaEvent
{
    private final Reason reason;
    private static final HandlerList handlers;
    
    public PersonaActivateEvent(final Persona persona, final Reason reason) {
        super(persona);
        this.reason = reason;
    }
    
    public Reason getReason() {
        return this.reason;
    }
    
    public HandlerList getHandlers() {
        return PersonaActivateEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PersonaActivateEvent.handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
    
    public enum Reason
    {
        LOGIN, 
        SWITCH;
    }
}
