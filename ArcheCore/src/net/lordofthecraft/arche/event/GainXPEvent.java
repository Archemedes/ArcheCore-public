package net.lordofthecraft.arche.event;

import org.bukkit.event.*;
import net.lordofthecraft.arche.interfaces.*;

public class GainXPEvent extends SkillEvent implements Cancellable
{
    private static final HandlerList handlers;
    private double xp;
    private boolean cancelled;
    
    public HandlerList getHandlers() {
        return GainXPEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return GainXPEvent.handlers;
    }
    
    public GainXPEvent(final Persona persona, final Skill skill, final double xp) {
        super(persona, skill);
        this.cancelled = false;
        this.xp = xp;
    }
    
    public double getAmountGained() {
        return this.xp;
    }
    
    public void setAmountGained(final double xp) {
        this.xp = xp;
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
