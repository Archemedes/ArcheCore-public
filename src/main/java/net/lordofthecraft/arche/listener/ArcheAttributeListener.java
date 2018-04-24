package net.lordofthecraft.arche.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
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
	
	@EventHandler(ignoreCancelled = true)
	public void regen(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			Persona ps = ArcheCore.getPersona(p);
			if (ps != null) {
				double regenFactor = AttributeRegistry.REGENERATION.getValue(ps);
				if(regenFactor != 1.0 && e.getAmount() > 0 && e.getRegainReason() != RegainReason.CUSTOM) {
					if(regenFactor < 0 ) regenFactor = 0;
					double gain = e.getAmount() * regenFactor;
					e.setAmount(gain);
				}
			}
		}
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
	public void on(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			Persona ps = ArcheCore.getPersona(p);
			if (ps != null) {
				ArcheAttribute a = null;
				
				switch(e.getCause()) {
				case FIRE: case FIRE_TICK:
					a = AttributeRegistry.FIRE_RESISTANCE;
					break;
				case POISON:
					a = AttributeRegistry.POISON_RESISTANCE;
					break;
				case WITHER:
					a = AttributeRegistry.WITHER_RESISTANCE;
					break;
				case MAGIC:
					a = AttributeRegistry.MAGIC_RESISTANCE;
					break;
				case DROWNING:
					a = AttributeRegistry.DROWNING_RESISTANCE;
					break;
				case BLOCK_EXPLOSION: case ENTITY_EXPLOSION:
					a = AttributeRegistry.BLAST_RESISTANCE;
					break;
				case PROJECTILE:
					a = AttributeRegistry.PROJECTILE_RESISTANCE;
					break;
					
					default: break;
				}
				
				if(a != null) {
					double resistFactor = Math.max(0.0, 2.0 - a.getValue(ps));
					if(resistFactor != 1.0) e.setDamage(e.getDamage() * resistFactor);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void ow(EntityDamageByEntityEvent e) {
		Entity damager = e.getDamager();
		Entity damagee = e.getEntity();
		if(damager instanceof Projectile) {
			handleProjectile(e);
		} else if(damager instanceof LivingEntity && damagee instanceof Player && !(damager instanceof Player)){
			handleMobDamage(e, (Player) damagee, AttributeRegistry.MOB_DAMAGE_TAKE);
		} else if (damager instanceof Player && damagee instanceof LivingEntity && !(damagee instanceof Player)) {
			handleMobDamage(e, (Player) damager, AttributeRegistry.MOB_DAMAGE_DEAL);
		}
	}
	
	private void handleProjectile(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Arrow) {
		//Arrow damage from players can be influenced
			Arrow a = (Arrow) e.getDamager();
			ProjectileSource shooter = a.getShooter();
			if(shooter instanceof Player) {
				Persona ps = ArcheCore.getPersona((Player) shooter);
				if(ps != null) {
					double mod = ps.attributes().getAttributeValue(AttributeRegistry.ARROW_DAMAGE);
					if(e.getEntity() instanceof LivingEntity && !(e.getEntity() instanceof Player))
						mod *= ps.attributes().getAttributeValue(AttributeRegistry.MOB_DAMAGE_DEAL);
					if(mod != 1.0) {
						double newDmg = e.getDamage() * mod;
						e.setDamage(newDmg);
					}
				}
			}
		} 
		
		//All projectiles from mobs can be influenced by Damage Taken From Mobs
		Projectile p = (Projectile) e.getDamager();
		ProjectileSource s = p.getShooter();
		if(e.getEntity() instanceof Player && s instanceof LivingEntity && !(s instanceof Player) ) {
			handleMobDamage(e, (Player) e.getEntity(), AttributeRegistry.MOB_DAMAGE_TAKE);
		}
		
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void food(FoodLevelChangeEvent e){
		Player p = (Player) e.getEntity();
		Persona ps = ArcheCore.getPersonaControls().getPersona(p);

		if(ps != null) {
			int change = e.getFoodLevel() - p.getFoodLevel();
			if(change > 0) { //Food level INCREASE probably ate something
				double exhaustion = Math.max(0, 
						Math.min(100, ps.attributes().getAttributeValue(AttributeRegistry.EXHAUSTION)) / 100.0);
				float saturation = p.getSaturation();
				
				Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), 
						()-> {
							float newSat = p.getSaturation();
							float satChange = newSat - saturation;
							double toDecrease = exhaustion * satChange;
							p.setSaturation((float) (newSat - toDecrease));
						});
			} else { //Food level DECREASE probably ticked down
				float mcExhaust = 0f;
				
				double hungerFactor = AttributeRegistry.HUNGER.getValue(ps);
				double exhaustion = AttributeRegistry.EXHAUSTION.getValue(ps);
				
				mcExhaust += hungerFactor;
				
				if(p.isSprinting() && exhaustion > 0) {
					int divider = 15;
					 mcExhaust += (exhaustion / divider);
				}
				
				if(mcExhaust <= 0) {
					e.setCancelled(true);
				} else if(mcExhaust != 1.0) {
					float newExhaustion = 4.0f - (4.0f / mcExhaust);
					p.setExhaustion(p.getExhaustion() + newExhaustion);
				}
				
			}
		}
	}
	
	//This nerfs the health regen gotten when full sat and food but low health
	//Nothing to do with exhaustion/atts but something that should be done anyway
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void foodHeal(EntityRegainHealthEvent e) {
		if(e.getRegainReason() == RegainReason.SATIATED && e.getEntityType() == EntityType.PLAYER ) {
			Player p = (Player) e.getEntity();
			if(p.getFoodLevel() == 20 && p.getSaturation() > 0) {
				e.setAmount(0.5 * e.getAmount());
			}
		}
	}
	
	
	private void handleMobDamage(EntityDamageByEntityEvent e, Player p, ArcheAttribute a) {
		Persona ps = ArcheCore.getPersona(p);
		if(ps != null) {
			double mod = ps.attributes().getAttributeValue(a);
			if(mod != 1.0) e.setDamage(e.getDamage() * mod);
		}
	}
	
}
