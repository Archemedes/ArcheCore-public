package net.lordofthecraft.arche.listener;

import io.github.archemedes.customitem.Customizer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class DebugListener implements Listener{
	
	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent e) {
		Player p = e.getPlayer();
		if (p.getEquipment().getItemInMainHand() != null) {
			if (p.getEquipment().getItemInMainHand().getType() == Material.STICK) {
				ItemStack is = p.getEquipment().getItemInMainHand();
				if (Customizer.isCustom(is)) {
					if (Customizer.getCustomTag(is).equalsIgnoreCase("entitydebugger")
							&& p.hasPermission("archecore.admin")) {
						p.sendMessage(String.valueOf(e.getRightClicked()));
					}
				}
			}
		}
	}

}
