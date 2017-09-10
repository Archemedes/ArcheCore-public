package net.lordofthecraft.arche.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.ArcheAttributeInstance;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.persona.PersonaAttributes;

public class ArcheAttributeListener implements Listener {
	private final PersonaHandler handler;
	
	public ArcheAttributeListener() {
		handler = ArcheCore.getControls().getPersonaHandler();
	}
	
	@EventHandler
	public void die(PlayerDeathEvent e) {
		Player p = e.getEntity();
		Persona persona = handler.getPersona(p);
		if(persona != null) {
			
			PersonaAttributes pa = persona.attributes();
			for(ArcheAttribute a : pa.getExistingInstances()) {
				ArcheAttributeInstance i = pa.getInstance(a);
				i.getModifiers().stream()
				.map(ExtendedAttributeModifier.class::cast)
				.filter(ExtendedAttributeModifier::isLostOnDeath)
				.forEach(m -> pa.removeModifier(a, m));
			}
		}
	}
}
