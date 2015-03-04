package net.lordofthecraft.arche.event;

import org.bukkit.event.player.*;
import net.lordofthecraft.arche.interfaces.*;

public abstract class PersonaEvent extends PlayerEvent
{
    protected final Persona persona;
    
    public PersonaEvent(final Persona persona) {
        super(persona.getPlayer());
        this.persona = persona;
    }
    
    public Persona getPersona() {
        return this.persona;
    }
}
