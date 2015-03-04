package net.lordofthecraft.arche.event;

import org.bukkit.event.*;
import net.lordofthecraft.arche.interfaces.*;

public class PersonaRenameEvent extends PersonaEvent implements Cancellable
{
    private static final HandlerList handlers;
    private String newName;
    private boolean cancelled;
    
    public PersonaRenameEvent(final Persona persona, final String newName) {
        super(persona);
        this.cancelled = false;
        this.newName = newName;
    }
    
    public String getNewName() {
        return this.newName;
    }
    
    public void setNewName(final String newName) {
        this.newName = newName;
    }
    
    public HandlerList getHandlers() {
        return PersonaRenameEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PersonaRenameEvent.handlers;
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
