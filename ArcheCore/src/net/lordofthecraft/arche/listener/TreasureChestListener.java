package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.TreasureChest;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class TreasureChestListener implements Listener {

	
	private void update(Player p){
		p.updateInventory();
	}
	
	@EventHandler
	public void onClick(PlayerInteractEvent e){
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
			Player p = e.getPlayer();
			ItemStack is = p.getItemInHand();
			if(is.isSimilar(TreasureChest.giveChest())){
				e.setCancelled(true);
				
				//Deduct an item
				if(is.getAmount() == 1) is.setType(Material.AIR);
				else is.setAmount(is.getAmount() - 1);
				p.setItemInHand(is);
				
				//Give message
				p.sendMessage(ChatColor.GREEN + "You manage to pry open the casket...");
				
				//Present the item(s)
				TreasureChest.giveLoot(p);
				update(p);
			}
		}
	}
}
