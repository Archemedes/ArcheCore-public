package net.lordofthecraft.arche.listener;

import org.bukkit.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import net.lordofthecraft.arche.*;
import org.bukkit.*;
import org.bukkit.inventory.*;
import org.bukkit.event.*;

public class TreasureChestListener implements Listener
{
    private void update(final Player p) {
        p.updateInventory();
    }
    
    @EventHandler
    public void onClick(final PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final Player p = e.getPlayer();
            final ItemStack is = p.getItemInHand();
            if (is.isSimilar(TreasureChest.giveChest())) {
                e.setCancelled(true);
                if (is.getAmount() == 1) {
                    is.setType(Material.AIR);
                }
                else {
                    is.setAmount(is.getAmount() - 1);
                }
                p.setItemInHand(is);
                p.sendMessage(ChatColor.GREEN + "You manage to pry open the casket...");
                TreasureChest.giveLoot(p);
                this.update(p);
            }
        }
    }
}
