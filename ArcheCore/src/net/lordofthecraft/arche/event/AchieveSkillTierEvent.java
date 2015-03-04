package net.lordofthecraft.arche.event;

import net.lordofthecraft.arche.enums.*;
import org.bukkit.event.*;
import net.lordofthecraft.arche.interfaces.*;

public class AchieveSkillTierEvent extends SkillEvent
{
    private final SkillTier old;
    private final SkillTier now;
    private static final HandlerList handlers;
    
    public AchieveSkillTierEvent(final Persona persona, final Skill skill, final SkillTier old, final SkillTier now) {
        super(persona, skill);
        this.old = old;
        this.now = now;
    }
    
    public SkillTier getFormerTier() {
        return this.old;
    }
    
    public SkillTier getAchievedTier() {
        return this.now;
    }
    
    public HandlerList getHandlers() {
        return AchieveSkillTierEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return AchieveSkillTierEvent.handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}
