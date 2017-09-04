package net.lordofthecraft.arche.persona;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.util.InventoryUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersonaInventory {
    private ItemStack[] contents;

    private PersonaInventory(ItemStack[] contents) {
        this.contents = contents;
    }

    public static PersonaInventory restore(String str) throws InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();

        config.loadFromString(str);
        if(config.getKeys(false).contains("contents")) {
            //501 - This is giving me compile errors. Incompatible types: Object cannot be cast to List<ItemStack>.
            @SuppressWarnings("unchecked")
			List<ItemStack> result = config.getList("contents").stream()
                    .map(ent -> (Map<String, Object>) ent)
                    .map(ItemStack::deserialize)
                    .collect(Collectors.toList());
        	ItemStack[] contents = result.toArray(new ItemStack[result.size()]);
            return new PersonaInventory(contents);
        } else throw new InvalidConfigurationException("Config node 'contents' not found! Should always be there and should always be the only tag!");
    }

    public static PersonaInventory store(Player p) {
    	PlayerInventory pinv = p.getInventory();
    	return InventoryUtil.isEmpty(pinv)?
    			null : new PersonaInventory(pinv.getContents());
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public PersonaInventory setContents(ItemStack[] contents) {
        this.contents = contents;
        return this;
    }

    public String getAsString() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> contentslist = Lists.newArrayList();
        for (ItemStack i : contents) {
        	if(i == null) contentslist.add(null);
        	else contentslist.add(i.serialize());
        }
        config.set("contents", contentslist);
        return config.saveToString();
    }
}
