package net.lordofthecraft.arche.listener;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.ArcheAttributeInstance;
import net.lordofthecraft.arche.attributes.AttributeRegistry;
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
	
	@EventHandler(ignoreCancelled = true)
	public void shoot(EntityShootBowEvent e) {
		if(e.getEntityType() == EntityType.PLAYER) {
			Player p = (Player) e.getEntity();
			Persona ps = ArcheCore.getPersona(p);
			if(ps != null) {
				double mod = ps.attributes().getAttributeValue(AttributeRegistry.ARROW_VELOCITY);
				if(mod != 1.0) {
					Entity proj = e.getProjectile();
					proj.setVelocity(proj.getVelocity().multiply(mod));
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void ow(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		if(damager instanceof Arrow) {
			Arrow a = (Arrow) damager;
			ProjectileSource shooter = a.getShooter();
			if(shooter instanceof Player) {
				Persona ps = ArcheCore.getPersona((Player) shooter);
				if(ps != null) {
					double mod = ps.attributes().getAttributeValue(AttributeRegistry.ARROW_DAMAGE);
					if(mod != 1.0) {
						double newDmg = e.getDamage() * mod;
						e.setDamage(newDmg);
					}
				}
			}
		}
	}
	
	
}
