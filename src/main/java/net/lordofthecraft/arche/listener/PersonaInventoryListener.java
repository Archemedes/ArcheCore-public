package net.lordofthecraft.arche.listener;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.persona.AttributeSelectMenu;
import net.lordofthecraft.arche.persona.PersonaInventory.PersonaInventoryHolder;
import net.lordofthecraft.arche.save.rows.persona.UpdateInventoryRow;
import net.lordofthecraft.arche.util.InventoryUtil;

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
  
    @EventHandler
    public void close(InventoryCloseEvent e) {
    	if (e.getInventory().getHolder() instanceof PersonaInventoryHolder) {
    		PersonaInventoryHolder holder = (PersonaInventoryHolder) e.getInventory().getHolder();
    		ArcheCore.getConsumerControls().queueRow(new UpdateInventoryRow(holder.getPersonaInventory()));
    		
        }
    }
}
