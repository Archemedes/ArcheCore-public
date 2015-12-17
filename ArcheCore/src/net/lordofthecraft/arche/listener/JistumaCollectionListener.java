package net.lordofthecraft.arche.listener;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class JistumaCollectionListener implements Listener{

	@EventHandler
	public void onDrinkingPotion (PlayerItemConsumeEvent e){
		ItemStack s = e.getItem();
		final Player p = e.getPlayer();
		if (s.getType()==Material.POTION){
			List<?> lore = s.getItemMeta().getLore();
			if (lore==null && !p.hasPermission("potions.mayuse")){
				e.setCancelled(true);
				p.setItemInHand(new ItemStack(Material.GLASS_BOTTLE,1));
			}
		}
	}
	
}
