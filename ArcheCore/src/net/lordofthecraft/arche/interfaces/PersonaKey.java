package net.lordofthecraft.arche.interfaces;

import java.util.*;

public interface PersonaKey
{
    UUID getPlayerUUID();
    
    int getPersonaId();
    
    Persona getPersona();
}
