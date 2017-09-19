package net.lordofthecraft.arche.persona;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.util.InventoryUtil;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PersonaInventory {
    private ItemStack[] contents;
    private ItemStack[] enderContents;

    public PersonaInventory(ItemStack[] contents, ItemStack[] enderContents) {
        this.contents = contents;
        this.enderContents = enderContents;
    }

    public static PersonaInventory restore(String inv, String enderinv) throws InvalidConfigurationException {
        YamlConfiguration config = new YamlConfiguration();
        YamlConfiguration enderconfig = new YamlConfiguration();

        config.loadFromString(inv);
        enderconfig.loadFromString(enderinv);
        if(config.getKeys(false).contains("contents")) {
            @SuppressWarnings("unchecked")
			List<ItemStack> result = config.getList("contents").stream()
                    .map(ent -> (Map<String, Object>) ent)
                    .map(ItemStack::deserialize)
                    .collect(Collectors.toList());
        	ItemStack[] contents = result.toArray(new ItemStack[result.size()]);
            if (enderconfig.getKeys(false).contains("contents")) {
                List<ItemStack> enderresult = config.getList("contents").stream()
                        .map(ent -> (Map<String, Object>) ent)
                        .map(ItemStack::deserialize)
                        .collect(Collectors.toList());
                ItemStack[] endercontents = enderresult.toArray(new ItemStack[enderresult.size()]);
                return new PersonaInventory(contents, endercontents);
            }
            return new PersonaInventory(contents, new ItemStack[InventoryType.ENDER_CHEST.getDefaultSize()]);
        } else throw new InvalidConfigurationException("Config node 'contents' not found! Should always be there and should always be the only tag!");

    }

    public static PersonaInventory store(Player p) {
    	PlayerInventory pinv = p.getInventory();
        return InventoryUtil.isEmpty(pinv) && InventoryUtil.isEmpty(p.getEnderChest()) ?
                null : new PersonaInventory(pinv.getContents(), p.getEnderChest().getContents());
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public ItemStack[] getEnderContents() {
        return enderContents;
    }

    public void setContents(ItemStack[] contents) {
        this.contents = contents;
    }

    public void setEnderContents(ItemStack[] contents) {
        this.enderContents = contents;
    }

    public String getInvAsString() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> contentslist = Lists.newArrayList();
        for (ItemStack i : contents) {
        	if(i == null) contentslist.add(null);
        	else contentslist.add(i.serialize());
        }
        config.set("contents", contentslist);
        return config.saveToString();
    }

    public String getEnderInvAsString() {
        YamlConfiguration config = new YamlConfiguration();
        List<Map<String, Object>> contentslist = Lists.newArrayList();
        for (ItemStack i : enderContents) {
            if (i == null) contentslist.add(null);
            else contentslist.add(i.serialize());
        }
        config.set("contents", contentslist);
        return config.saveToString();
    }

    @Override
    public String toString() {
        return "PersonaInventory{" +
                "contents=" + getInvAsString() +
                ", enderContents=" + getEnderInvAsString() +
                '}';
    }
}
