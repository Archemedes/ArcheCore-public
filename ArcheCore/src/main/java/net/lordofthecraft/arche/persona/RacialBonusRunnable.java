package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class RacialBonusRunnable extends BukkitRunnable {
	PersonaHandler handle = ArchePersonaHandler.getInstance();
	
	@Override
	public void run() {
		PotionEffect pe = new PotionEffect(PotionEffectType.INVISIBILITY, 200, 2, true, true);
		for(World w : Bukkit.getWorlds()){
			if(w.getEnvironment() == Environment.NORMAL && (w.getTime() > 23000 || w.getTime() < 13000) ){
				for(Player p : w.getPlayers()){
					Persona ps = handle.getPersona(p);
					if(ps != null && ps.getRace() == Race.SPECTRE && p.getEyeLocation().getBlock().getLightFromSky() > 6){
						p.addPotionEffect(pe);
						@SuppressWarnings("deprecation")
						EntityDamageEvent event = new EntityDamageEvent(p, DamageCause.FIRE_TICK, 2);
						Bukkit.getPluginManager().callEvent(event);
						if(!event.isCancelled()) p.damage(event.getDamage());
					}
				}
			}
		}
	}

}
