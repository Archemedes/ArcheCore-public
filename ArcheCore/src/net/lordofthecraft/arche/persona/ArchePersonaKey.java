package net.lordofthecraft.arche.persona;

import java.util.*;
import org.apache.commons.lang.*;
import net.lordofthecraft.arche.interfaces.*;

public class ArchePersonaKey implements PersonaKey
{
    private final UUID uuid;
    private final int id;
    
    public ArchePersonaKey(final UUID uuid, final int id) {
        super();
        Validate.notNull((Object)uuid, "Personas must have UUID");
        this.uuid = uuid;
        this.id = id;
    }
    
    @Override
    public UUID getPlayerUUID() {
        return this.uuid;
    }
    
    @Override
    public int getPersonaId() {
        return this.id;
    }
    
    @Override
    public Persona getPersona() {
        return ArchePersonaHandler.getInstance().getPersona((PersonaKey)this);
    }
    
    @Override
    public int hashCode() {
        return this.id + this.uuid.hashCode();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof ArchePersonaKey)) {
            return false;
        }
        final ArchePersonaKey other = (ArchePersonaKey)o;
        return this.id == other.id && this.uuid.equals(other.uuid);
    }
    
    @Override
    public String toString() {
        return "ArchePersonaKey:" + this.uuid + "@" + this.id;
    }
}
