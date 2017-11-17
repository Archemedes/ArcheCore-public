package net.lordofthecraft.arche.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.persona.PersonaInventory.PersonaInventoryHolder;
import net.lordofthecraft.arche.save.rows.persona.UpdateInventoryRow;

public class PersonaInventoryListener implements Listener {

    public PersonaInventoryListener() {}
  
    @EventHandler
    public void close(InventoryCloseEvent e) {
    	if (e.getInventory().getHolder() instanceof PersonaInventoryHolder) {
    		PersonaInventoryHolder holder = (PersonaInventoryHolder) e.getInventory().getHolder();
    		ArcheCore.getConsumerControls().queueRow(new UpdateInventoryRow(holder.getPersonaInventory()));
    		
        }
    }
}
