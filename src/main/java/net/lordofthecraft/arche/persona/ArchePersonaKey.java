package net.lordofthecraft.arche.persona;

import java.util.UUID;

import lombok.Value;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;

@Value
public class ArchePersonaKey implements PersonaKey {
		int personaId;
		UUID playerUUID;
		int personaSlot;


		@Override
		public Persona getPersona(){
			ArcheOfflinePersona p = getOfflinePersona();
			if(p instanceof ArchePersona) return (ArchePersona) p;
			else return null;
		}

		@Override
		public ArcheOfflinePersona getOfflinePersona() {
			return ArchePersonaHandler.getInstance().getPersonaById(personaId);
		}

		@Override
		public int hashCode(){
			return personaId;
		}

		@Override
		public boolean equals(Object o){
			if(o == null || !(o instanceof ArchePersonaKey)) return false;

			ArchePersonaKey other = (ArchePersonaKey) o;
			return personaId == other.personaId;
		}

		@Override
		public String toString(){
			return "ArchePersonaKey:" + playerUUID + "@" + personaSlot + "[PID:" + personaId + "]";
		}

}
