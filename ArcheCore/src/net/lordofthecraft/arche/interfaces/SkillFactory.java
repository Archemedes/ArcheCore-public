package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.enums.*;
import org.bukkit.*;

public interface SkillFactory
{
    SkillFactory withVisibilityType(int p0);
    
    SkillFactory withXpGainWhileHidden(boolean p0);
    
    SkillFactory asRacialProfession(Race p0);
    
    SkillFactory withRacialModifier(Race p0, double p1);
    
    SkillFactory withHelpFile(String p0, Material p1);
    
    SkillFactory setIntensiveProfession(boolean p0);
    
    Skill register();
}
