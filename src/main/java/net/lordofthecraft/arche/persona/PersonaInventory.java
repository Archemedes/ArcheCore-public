
package net.lordofthecraft.arche.persona;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;

public class PersonaInventory {
	private final OfflinePersona persona;
	private final Inventory inv;
	private final Inventory enderInv;

    public PersonaInventory(OfflinePersona persona, ItemStack[] contents, ItemStack[] enderContents) {
    	this.persona = persona;
    	inv = PersonaInventoryHolder.get(this, InventoryType.PLAYER);
    	inv.setContents(contents);
    	enderInv = PersonaInventoryHolder.get(this, InventoryType.ENDER_CHEST);
    	enderInv.setContents(enderContents);
    }

    @SuppressWarnings("unchecked")
	public static PersonaInventory restore(OfflinePersona persona, String inv, String enderinv) {
    	YamlConfiguration config = new YamlConfiguration();
    	ItemStack[] contents, enderContents;
    	try {
    		if(inv != null) {
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

    	} catch(InvalidConfigurationException e) {
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
    
    public OfflinePersona getPersona() {
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
        YamlConfiguration config = new YamlConfiguration();
        ItemStack[] contents = someInv.getContents();
        List<Map<String, Object>> contentslist = Lists.newArrayList();
        for (ItemStack i : contents) {
        	if(i == null) contentslist.add(null);
        	else contentslist.add(i.serialize());
        }
        config.set("c", contentslist);
        return config.saveToString();
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
    	public Inventory getInventory() {return inv;}
    	public PersonaInventory getPersonaInventory() { return pinv; } 
    }
}
