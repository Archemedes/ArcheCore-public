package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import org.apache.commons.lang.Validate;

import java.util.UUID;

public class ArchePersonaKey implements PersonaKey {
	private final UUID uuid;
    private final int slot;
    private final int persona_id;

    public ArchePersonaKey(int persona_id, UUID uuid, int slot) {
        Validate.notNull(uuid, "Personas must have UUID");
		this.uuid = uuid;
        this.persona_id = persona_id;
        this.slot = slot;
    }
	
	@Override
	public UUID getPlayerUUID(){
		return uuid;
	}
	
	@Override
    public int getPersonaID() {
        return persona_id;
    }

    @Override
    public int getPersonaSlot() {
        return slot;
    }

    @Override
    public Persona getPersona(){
        return ArchePersonaHandler.getInstance().getPersona(uuid, slot);
    }

    @Override
	public int hashCode(){
        return slot + uuid.hashCode();
    }
	
	@Override
	public boolean equals(Object o){
		if(o == null || !(o instanceof ArchePersonaKey)) return false;
		
		ArchePersonaKey other = (ArchePersonaKey) o;
        return persona_id == other.persona_id;
    }
	
	@Override
	public String toString(){
        return "ArchePersonaKey:" + uuid + "@" + slot + "[PID:" + persona_id + "]";
    }
	
}
