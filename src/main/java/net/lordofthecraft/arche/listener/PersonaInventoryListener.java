package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.PersonaInventory;
import net.lordofthecraft.arche.persona.PersonaInventory.PersonaInventoryHolder;
import net.lordofthecraft.arche.save.rows.persona.UpdateInventoryRow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

public class PersonaInventoryListener implements Listener {

    public PersonaInventoryListener() {
    }
  
    @EventHandler
    public void close(InventoryCloseEvent e) {
        if (e.getInventory().getHolder() instanceof PersonaInventoryHolder) {
            PersonaInventoryHolder holder = (PersonaInventoryHolder) e.getInventory().getHolder();
            PersonaInventory pinv = holder.getPersonaInventory();
            ArcheCore.getConsumerControls().queueRow(new UpdateInventoryRow(pinv));

            Persona persona = pinv.getPersona();
            if (persona.isCurrent()) {
                Player player = persona.getPlayer();
                if (player != null) { //Player may have come online in the meantime
                    if (e.getInventory().getType() == InventoryType.ENDER_CHEST) {
                        player.getEnderChest().setContents(pinv.getEnderContents());
                        player.sendMessage("Your enderchest has been updated through administrative action");
                    } else {
                        player.getInventory().setContents(pinv.getContents());
                        player.sendMessage("Your inventory has been updated through administrative action");
                    }
                } else {
                    persona.tags().giveTag("refreshMCSpecifics", "true");
                }
            }
        }
    }
}
