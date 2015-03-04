package net.lordofthecraft.arche.event;

import org.bukkit.event.*;
import net.lordofthecraft.arche.interfaces.*;

public class PersonaRemoveEvent extends PersonaDeactivateEvent implements Cancellable
{
    private static final HandlerList handlers;
    private final boolean makeRoom;
    private boolean cancelled;
    
    public PersonaRemoveEvent(final Persona persona, final boolean makeRoom) {
        super(persona, Reason.REMOVE);
        this.cancelled = false;
        this.makeRoom = makeRoom;
    }
    
    @Override
    public HandlerList getHandlers() {
        return PersonaRemoveEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return PersonaRemoveEvent.handlers;
    }
    
    public boolean isToMakeRoom() {
        return this.makeRoom;
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
