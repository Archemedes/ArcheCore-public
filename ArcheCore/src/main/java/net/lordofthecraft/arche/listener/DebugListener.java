package net.lordofthecraft.arche.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import io.github.archemedes.customitem.Customizer;

public class DebugListener implements Listener{
	
	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent e) {
		Player p = e.getPlayer();
		if (p.getItemInHand() != null) {
			if (p.getItemInHand().getType() == Material.STICK) {
				ItemStack is = p.getItemInHand();
				if (Customizer.isCustom(is)) {
					if (Customizer.getCustomTag(is).equalsIgnoreCase("entitydebugger")) {
						p.sendMessage(e.getRightClicked().toString());
					}
				}
			}
		}
	}

}
