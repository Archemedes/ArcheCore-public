package net.lordofthecraft.arche.interfaces;

import java.util.*;
import org.bukkit.entity.*;
import net.lordofthecraft.arche.enums.*;

public interface Persona
{
    Skill getProfession(ProfessionSlot p0);
    
    void setProfession(ProfessionSlot p0, Skill p1);
    
    Skill getMainSkill();
    
    void setMainSkill(Skill p0);
    
    int getId();
    
    boolean isCurrent();
    
    void setPrefix(String p0);
    
    String getPrefix();
    
    boolean hasPrefix();
    
    void clearPrefix();
    
    void setXPGain(boolean p0);
    
    boolean getXPGain();
    
    int getTimePlayed();
    
    int getCharactersSpoken();
    
    String getPlayerName();
    
    PersonaKey getPersonaKey();
    
    UUID getPlayerUUID();
    
    Player getPlayer();
    
    String getChatName();
    
    String getName();
    
    Race getRace();
    
    String getRaceString();
    
    void setApparentRace(String p0);
    
    long getRenamed();
    
    void setName(String p0);
    
    void clearDescription();
    
    void setDescription(String p0);
    
    void addDescription(String p0);
    
    String getDescription();
    
    String getGender();
    
    int getAge();
    
    void setAge(int p0);
    
    boolean doesAutoAge();
    
    void setAutoAge(boolean p0);
    
    boolean remove();
}
