package net.lordofthecraft.arche.interfaces;

import java.util.*;
import net.lordofthecraft.arche.SQL.*;
import net.lordofthecraft.arche.help.*;
import org.bukkit.inventory.*;
import org.bukkit.block.*;
import net.lordofthecraft.arche.*;
import org.bukkit.*;

public interface IArcheCore
{
    PersonaHandler getPersonaHandler();
    
    PersonaKey composePersonaKey(UUID p0, int p1);
    
    BlockRegistry getBlockRegistry();
    
    SQLHandler getSQLHandler();
    
    void addHelp(HelpFile p0);
    
    void addHelp(String p0, String p1);
    
    void addHelp(String p0, String p1, Material p2);
    
    void addInfo(String p0, String p1);
    
    Skill getSkill(String p0);
    
    Skill createSkill(String p0);
    
    SkillFactory registerNewSkill(String p0);
    
    ItemStack giveSkillTome(Skill p0);
    
    ItemStack giveTreasureChest();
    
    boolean areRacialBonusesEnabled();
    
    boolean arePrefixesEnabled();
    
    int getNewbieProtectDelay();
    
    boolean willModifyDisplayNames();
    
    boolean isBlockPlayerPlaced(Block p0);
    
    boolean willLogUsernames();
    
    UsernameLogger getUsernameLogger();
    
    boolean willCachePersonas();
    
    boolean usesEconomy();
    
    Economy getEconomy();
    
    boolean teleportNewPersonas();
    
    World getNewPersonaWorld();
}
