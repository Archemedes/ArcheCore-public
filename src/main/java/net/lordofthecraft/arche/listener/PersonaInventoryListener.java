package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.AttributeSelectMenu;
import net.lordofthecraft.arche.util.InventoryUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PersonaInventoryListener implements Listener {

    public PersonaInventoryListener() {
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof Persona) {
            if (e.getWhoClicked().hasPermission("archecore.mod")) {
                Persona pers = (Persona) e.getInventory().getHolder();
                if (pers != null) {
                    Inventory inv = e.getInventory();
                    if (inv.getContents() != pers.getInventory().getContents()) {
                        ItemStack[] contents = new ItemStack[e.getWhoClicked().getInventory().getSize()];
                        for (int i = 0; i < e.getWhoClicked().getInventory().getSize(); ++i) {
                            contents[i] = inv.getContents()[i];
                        }
                        if (e.getInventory().getTitle().contains("ender")) {
                            pers.getPInv().setEnderContents(contents);
                        } else {
                            pers.getPInv().setContents(contents);
                        }
                    }

                }
            } else {
                e.setCancelled(true);
            }
        } else if (e.getInventory().getTitle().contains("Casket Table(Pg.")) {
            e.setCancelled(true);
        } else if (e.getInventory().getHolder() instanceof AttributeSelectMenu) {
            AttributeSelectMenu holder = (AttributeSelectMenu) e.getInventory().getHolder();
            List<InventoryUtil.MovedItem> moved = InventoryUtil.getResultOfEvent(e);
            e.setCancelled(true);
            moved.stream().map(InventoryUtil.MovedItem::getInitialSlot).forEach(holder::click);
        }
    }
}
