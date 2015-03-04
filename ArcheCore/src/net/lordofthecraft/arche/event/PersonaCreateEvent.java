package net.lordofthecraft.arche.event;

import net.lordofthecraft.arche.interfaces.*;
import org.bukkit.event.*;

public class PersonaCreateEvent extends PersonaEvent implements Cancellable
{
    private final Persona replaces;
    private static final HandlerList handlers;
    private boolean cancelled;
    
    public HandlerList getHandlers() {
        return PersonaCreateEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PersonaCreateEvent.handlers;
    }
    
    public PersonaCreateEvent(final Persona persona, final Persona replaces) {
        super(persona);
        this.cancelled = false;
        this.replaces = replaces;
    }
    
    public Persona getReplacedPersona() {
        return this.replaces;
    }
    
    public boolean isCancelled() {
        return this.cancelled;
    }
    
    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    static {
        handlers = new HandlerList();
    }
}
