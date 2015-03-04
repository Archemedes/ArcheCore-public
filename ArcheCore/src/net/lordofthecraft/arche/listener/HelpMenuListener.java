package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.help.*;
import org.bukkit.plugin.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

public class HelpMenuListener implements Listener
{
    private final HelpDesk helpdesk;
    private final Plugin plugin;
    
    public HelpMenuListener(final Plugin plugin, final HelpDesk helpdesk) {
        super();
        this.plugin = plugin;
        this.helpdesk = helpdesk;
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onDrag(final InventoryDragEvent e) {
        final Inventory inv = e.getInventory();
        if (inv.getTitle() == HelpDesk.HELP_HEADER) {
            e.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onClick(final InventoryClickEvent e) {
        final Inventory inv = e.getInventory();
        if (inv.getTitle() == HelpDesk.HELP_HEADER) {
            e.setCancelled(true);
            final int slot = e.getSlot();
            if (e.isLeftClick() && slot >= 0 && e.getRawSlot() == e.getSlot()) {
                final ItemStack i = inv.getItem(slot);
                if (i != null && i.hasItemMeta()) {
                    final ItemMeta meta = i.getItemMeta();
                    final String topic = meta.getDisplayName().substring(2);
                    final Player p = (Player)e.getWhoClicked();
                    if (meta.hasLore()) {
                        this.helpdesk.outputSkillHelp(topic, p);
                    }
                    else {
                        this.helpdesk.outputHelp(topic, p);
                    }
                    new BukkitRunnable() {
                        public void run() {
                            p.closeInventory();
                        }
                    }.runTask(this.plugin);
                }
            }
        }
    }
}
