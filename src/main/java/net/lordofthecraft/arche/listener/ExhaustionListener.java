package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.interfaces.Persona;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class ExhaustionListener implements Listener {
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void food(FoodLevelChangeEvent e){
		Player p = (Player) e.getEntity();
		Persona ps = ArcheCore.getPersonaControls().getPersona(p);
		
		if(ps != null) {
			int change = e.getFoodLevel() - p.getFoodLevel();
			if(change > 0) {
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
			} else if(p.isSprinting()) {
				double exhaustion = ps.attributes().getAttributeValue(AttributeRegistry.EXHAUSTION);
				if(exhaustion < 0) exhaustion = 0;
				int foodLevel = e.getFoodLevel();
				int divider = 15;
                double totalExtraFood = exhaustion / divider;
                int extraFood = (int) (exhaustion / divider);
                double remainder = totalExtraFood == 0? 0 : (totalExtraFood - extraFood) / totalExtraFood * 4.0;
                //System.out.println("Exhaustion: " + exhaustion + " " + extraFood + " " + remainder);
                foodLevel = Math.max(0, foodLevel - extraFood);
				e.setFoodLevel(foodLevel);
				p.setExhaustion((float) remainder);
			}
		}
	}
	
	//This nerfs the health regen gotten when full sat and food but low health
	//Nothing to do with exhaustion but something that should be done anyway
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void foodHeal(EntityRegainHealthEvent e) {
		if(e.getRegainReason() == RegainReason.SATIATED && e.getEntityType() == EntityType.PLAYER ) {
			Player p = (Player) e.getEntity();
			if(p.getFoodLevel() == 20 && p.getSaturation() > 0) {
				e.setAmount(0.5 * e.getAmount());
			}
		}
	}
}
