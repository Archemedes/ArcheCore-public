package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import org.apache.commons.lang.Validate;

import java.util.UUID;

public class ArchePersonaKey implements PersonaKey {
	private final UUID uuid;
	private final int id;
	
	public ArchePersonaKey(UUID uuid, int id){
		Validate.notNull(uuid, "Personas must have UUID");
		this.uuid = uuid;
		this.id = id;
	}
	
	@Override
	public UUID getPlayerUUID(){
		return uuid;
	}
	
	@Override
	public int getPersonaId(){
		return id;
	}
	
	@Override
	public Persona getPersona(){
		return ArchePersonaHandler.getInstance().getPersona(this);
	}
	
	@Override
	public int hashCode(){
		return id + uuid.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null || !(o instanceof ArchePersonaKey)) return false;
		
		ArchePersonaKey other = (ArchePersonaKey) o;
		return id == other.id && uuid.equals(other.uuid);
	}
	
	@Override
	public String toString(){
		return "ArchePersonaKey:" + uuid + "@" + id;
	}
	
}
