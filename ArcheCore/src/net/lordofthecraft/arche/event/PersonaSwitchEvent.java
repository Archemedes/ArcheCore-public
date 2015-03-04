package net.lordofthecraft.arche.event;

import org.bukkit.event.*;
import net.lordofthecraft.arche.interfaces.*;

public class PersonaSwitchEvent extends PersonaEvent implements Cancellable
{
    private static final HandlerList handlers;
    private boolean cancelled;
    
    public HandlerList getHandlers() {
        return PersonaSwitchEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PersonaSwitchEvent.handlers;
    }
    
    public PersonaSwitchEvent(final Persona persona) {
        super(persona);
        this.cancelled = false;
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
