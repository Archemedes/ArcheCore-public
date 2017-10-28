package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.persona.AttributeSelectMenu;
import net.lordofthecraft.arche.util.InventoryUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;

public class PersonaInventoryListener implements Listener {

    public PersonaInventoryListener() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
    	if (e.getInventory().getHolder() instanceof AttributeSelectMenu) {
            AttributeSelectMenu holder = (AttributeSelectMenu) e.getInventory().getHolder();
            List<InventoryUtil.MovedItem> moved = InventoryUtil.getResultOfEvent(e);
            e.setCancelled(true);
            moved.stream().map(InventoryUtil.MovedItem::getInitialSlot).forEach(holder::click);
        }
    }
}
