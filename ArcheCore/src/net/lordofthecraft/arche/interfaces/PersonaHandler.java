package net.lordofthecraft.arche.interfaces;

import org.bukkit.entity.*;
import org.bukkit.*;
import net.lordofthecraft.arche.enums.*;
import java.util.*;

public interface PersonaHandler
{
    void setModifyDisplayNames(boolean p0);
    
    boolean willModifyDisplayNames();
    
    boolean mayUse(Player p0);
    
    int getAllowedPersonas(Player p0);
    
    Persona getPersona(PersonaKey p0);
    
    Persona getPersona(UUID p0, int p1);
    
    Persona getPersona(OfflinePlayer p0);
    
    Persona getPersona(Player p0);
    
    boolean hasPersona(Player p0);
    
    Persona[] getAllPersonas(OfflinePlayer p0);
    
    Persona[] getAllPersonas(UUID p0);
    
    int countPersonas(UUID p0);
    
    int countPersonas(Player p0);
    
    void switchPersona(Player p0, int p1);
    
    Persona createPersona(Player p0, int p1, String p2, Race p3, int p4, int p5, boolean p6);
    
    List<String> whois(Persona p0);
    
    List<String> whois(Player p0);
    
    void ageUs();
}
