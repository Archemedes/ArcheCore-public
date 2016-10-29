package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.TreasureChest;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class TreasureChestListener implements Listener {


	private void update(Player p){
		p.updateInventory();
	}


	// Cancel placing of caskets
	@EventHandler(ignoreCancelled = true)
    public void onCasketPlace(BlockPlaceEvent event) {
        if (event.getItemInHand().isSimilar(TreasureChest.giveChest())) {
            event.setCancelled(true);
        }
    }

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onClick(PlayerInteractEvent e){
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK){
			Player p = e.getPlayer();
			ItemStack is = p.getEquipment().getItemInMainHand();
			if(is.isSimilar(TreasureChest.giveChest())){
				e.setCancelled(true);

				//Deduct an item
				if(is.getAmount() == 1) is.setType(Material.AIR);
				else is.setAmount(is.getAmount() - 1);
				p.getEquipment().setItemInMainHand(is);

				//Give message
				p.sendMessage(ChatColor.GREEN + "You manage to pry open the casket...");

				//Present the item(s)
				TreasureChest.giveLoot(p);
				update(p);
			}
		}
	}
}
