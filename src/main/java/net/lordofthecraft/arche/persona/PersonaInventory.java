
package net.lordofthecraft.arche.persona;

import co.lotc.core.bukkit.util.InventoryUtil;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.persona.InvDiffRow;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersonaInventory {
    private final Persona persona;
    private final Inventory inv;
    private final Inventory enderInv;

    private PersonaInventory(Persona persona, ItemStack[] contents, ItemStack[] enderContents) {
        this.persona = persona;
        inv = PersonaInventoryHolder.get(this, InventoryType.PLAYER);
        inv.setContents(contents);
        enderInv = PersonaInventoryHolder.get(this, InventoryType.ENDER_CHEST);
        enderInv.setContents(enderContents);
    }
    
    void saveDiff() { //Important this is run before the vitals update row
    	Inventory prsInv = Bukkit.createInventory(null, 54);
    	Inventory endInv = Bukkit.createInventory(null, 54);
    	prsInv.setContents(inv.getContents());
    	endInv.setContents(enderInv.getContents());
    	
    	new InvDiffRow(persona.getPersonaId(), prsInv,endInv).queue();
    }

    @SuppressWarnings("unchecked")
    public static PersonaInventory restore(Persona persona, String inv, String enderinv) {
        YamlConfiguration config = new YamlConfiguration();
        ItemStack[] contents, enderContents;
        try {
            if (inv != null) {
                config.loadFromString(inv);
                List<ItemStack> result = config.getList("c").stream()
                        .map(ent -> (Map<String, Object>) ent)
                        .map(ent -> ent == null ? null : ItemStack.deserialize(ent))
                        .collect(Collectors.toList());
                contents = result.toArray(new ItemStack[result.size()]);

            } else {
                contents = new ItemStack[InventoryType.PLAYER.getDefaultSize()];
            }

            if (enderinv != null) {
                config = new YamlConfiguration();
                config.loadFromString(enderinv);
                List<ItemStack> enderresult = config.getList("c").stream()
                        .map(ent -> (Map<String, Object>) ent)
                        .map(ent -> ent == null ? null : ItemStack.deserialize(ent))
                        .collect(Collectors.toList());
                enderContents = enderresult.toArray(new ItemStack[enderresult.size()]);
            } else {
                enderContents = new ItemStack[InventoryType.ENDER_CHEST.getDefaultSize()];
            }

        } catch (InvalidConfigurationException e) {
            contents = new ItemStack[InventoryType.PLAYER.getDefaultSize()];
            enderContents = new ItemStack[InventoryType.ENDER_CHEST.getDefaultSize()];
            e.printStackTrace();
        }

        return new PersonaInventory(persona, contents, enderContents);
    }

    public static PersonaInventory store(Persona persona) {
        Player p = persona.getPlayer();
        return new PersonaInventory(persona, p.getInventory().getContents(), p.getEnderChest().getContents());
    }

    public Persona getPersona() {
        return persona;
    }

    public ItemStack[] getContents() {
        return inv.getContents();
    }

    public ItemStack[] getEnderContents() {
        return enderInv.getContents();
    }

    public Inventory getInventory() {
        return inv;
    }

    public Inventory getEnderInventory() {
        return enderInv;
    }

    public String getInvAsString() {
        return getInvAsString(inv);
    }

    public String getEnderInvAsString() {
        return getInvAsString(enderInv);
    }

    private String getInvAsString(Inventory someInv) {
    	return InventoryUtil.serializeItems(someInv);
    }

    @Override
    public String toString() {
        return "PersonaInventory{" +
                "contents=" + getInvAsString() +
                ", enderContents=" + getEnderInvAsString() +
                '}';
    }

    public static class PersonaInventoryHolder implements InventoryHolder {
        public static Inventory get(PersonaInventory pinv, InventoryType type) {
            return new PersonaInventoryHolder(pinv, type).getInventory();
        }

        private PersonaInventoryHolder(PersonaInventory pinv, InventoryType t) {
            this.pinv = pinv;
            inv = Bukkit.createInventory(this, t, "Persona Inventory");
        }

        private final Inventory inv;
        private final PersonaInventory pinv;

        @Override
				public Inventory getInventory() {
            return inv;
        }

        public PersonaInventory getPersonaInventory() {
            return pinv;
        }
    }
}
