package net.lordofthecraft.arche.interfaces;

import org.bukkit.entity.*;
import net.lordofthecraft.arche.enums.*;

public interface Skill
{
    public static final int VISIBILITY_VISIBLE = 1;
    public static final int VISIBILITY_DISCOVERABLE = 2;
    public static final int VISIBILITY_GRANTABLE = 3;
    public static final int VISIBILITY_HIDDEN = 4;
    
    String getName();
    
    int getId();
    
    int getVisibility();
    
    boolean isInert();
    
    boolean isVisible(Player p0);
    
    boolean isVisible(Persona p0);
    
    boolean reveal(Persona p0);
    
    void addXp(Player p0, double p1);
    
    void addXp(Persona p0, double p1);
    
    void addRawXp(Player p0, double p1);
    
    void addRawXp(Player p0, double p1, boolean p2);
    
    void addRawXp(Persona p0, double p1);
    
    void addRawXp(Persona p0, double p1, boolean p2);
    
    double getXp(Player p0);
    
    double getXp(Persona p0);
    
    boolean achievedTier(Player p0, SkillTier p1);
    
    boolean achievedTier(Persona p0, SkillTier p1);
    
    SkillTier getSkillTier(Player p0);
    
    SkillTier getSkillTier(Persona p0);
    
    boolean canGainXp(Persona p0);
    
    boolean isProfessionFor(Race p0);
    
    SkillTier getCapTier(Persona p0);
    
    boolean isIntensiveProfession();
}
