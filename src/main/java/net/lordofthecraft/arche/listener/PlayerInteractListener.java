package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.ArcheBeacon;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractListener implements Listener {
	private final ArchePersonaHandler handler;
	
	public PlayerInteractListener(ArcheCore plugin){
		handler = plugin.getPersonaHandler();
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onClickPlayer(PlayerInteractEntityEvent e){
		Player p = e.getPlayer();
		if (e.getRightClicked() instanceof Player && p.isSneaking() && e.getHand() == EquipmentSlot.HAND) {
			Player target = (Player) e.getRightClicked();
			handler.whois(target, p.hasPermission("archecore.mod.other")).forEach(p::sendMessage);
		}
		
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClickBeacon(PlayerInteractEvent e){
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
			Material m = e.getClickedBlock().getType();
			Player p = e.getPlayer();
			if(m == Material.BEACON){
				if(!p.isSneaking() || !p.hasPermission("archecore.vanillabeacon")){
					e.setCancelled(true);
					
					if(p.hasPermission("archecore.mayuse")){
						ArcheBeacon.openBeacon(p);
					}
					
				}
			} else if( m == Material.ENDER_CHEST){
				if(!p.hasPermission("archecore.enderchest")){
					e.setCancelled(true);
					p.sendMessage(ChatColor.RED + "You do not have access to your Ender Chest.");
				}
			}
		}
	}
	
}
