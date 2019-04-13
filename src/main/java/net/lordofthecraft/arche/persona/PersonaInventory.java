
package net.lordofthecraft.arche.persona;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.util.InventoryUtil;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.persona.InvDiffRow;

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

    public static PersonaInventory restore(Persona persona, String inv, String enderinv) {
        ItemStack[] contents=null, enderContents=null;
        if (inv != null) contents = InventoryUtil.deserializeItems(inv).toArray(new ItemStack[0]);
        if (enderinv != null)	enderContents = InventoryUtil.deserializeItems(enderinv).toArray(new ItemStack[0]);

        if(contents == null) contents = new ItemStack[InventoryType.PLAYER.getDefaultSize()];
        if(enderContents == null) enderContents = new ItemStack[InventoryType.ENDER_CHEST.getDefaultSize()];

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
