package net.lordofthecraft.arche.listener;

import com.google.common.collect.Sets;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import java.util.Set;
import java.util.UUID;

public class NewbieProtectListener implements Listener {
	public static final Set<UUID> bonusProtects = Sets.newHashSet();
	
	private final ArchePersonaHandler handler;
	private final int protectDuration;
	
	public NewbieProtectListener(ArchePersonaHandler handler, int duration){
		this.handler = handler;
		protectDuration = duration;
	}
	
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent e){
		if(e.getEntity() instanceof Player){
			Player target = (Player) e.getEntity();
			if(bonusProtects.contains(target.getUniqueId())){
				target.sendMessage(ChatColor.GOLD + "The monks have deemed fit to guide you to safer places.");
				World w = ArcheCore.getControls().getNewPersonaWorld();
				Location to = w == null? target.getWorld().getSpawnLocation() : w.getSpawnLocation();
				target.teleport(to);
			}
			
			if(e.getCause() == DamageCause.MAGIC && isNewbie(target))
				e.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent e){
		
		if(e.getEntity() instanceof Player){
			Player target = (Player) e.getEntity();
			
			Entity d = e.getDamager();
			boolean newbieTarget = isNewbie(target);
			
			if(d instanceof Player){
				Player p = (Player) d;
				if(isNewbie(p)){
					p.sendMessage(ChatColor.LIGHT_PURPLE + "You must further attune to your Persona before picking fights");
					barrier(target, p);
					e.setCancelled(true);
				} else if(newbieTarget){
					p.sendMessage(ChatColor.LIGHT_PURPLE + "This Persona is shielded by a divine force... for now...");
					barrier(target, p);
					e.setCancelled(true);
				}
			}else if (d instanceof Projectile){
				if(((Projectile) d).getShooter() instanceof Player){
					Player p = (Player) ((Projectile) d).getShooter();
					if(isNewbie(p)){
						p.sendMessage(ChatColor.LIGHT_PURPLE + "You must further attune to your Persona before picking fights");
						barrier(target, p);
						e.setCancelled(true);
					} else if(newbieTarget){
						p.sendMessage(ChatColor.LIGHT_PURPLE + "This Persona is shielded by a divine force... for now...");
						barrier(target, p);
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	public boolean isNewbie(Player p){
		ArchePersona pers = handler.getPersona(p);
		return (!p.hasPermission("archecore.persona.nonnewbie") && pers != null && pers.getTimePlayed() < protectDuration);
	}
	
	private void barrier(Player target, Player damager){
        Location l = target.getLocation();
        Vector v = damager.getLocation().subtract(l).getDirection().multiply(0.5);
		l.add(v);

        damager.spawnParticle(Particle.BARRIER, l, 1);
    }
}
