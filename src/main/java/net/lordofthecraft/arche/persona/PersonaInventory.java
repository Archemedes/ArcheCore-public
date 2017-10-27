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

    @SuppressWarnings("unchecked")
    public static PersonaInventory restore(String inv, String enderinv) {
        YamlConfiguration config = new YamlConfiguration();
        ItemStack[] contents, enderContents;
        try {
            if (inv != null) {
                config.loadFromString(inv);
                List<ItemStack> result = config.getList("contents").stream()
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
                List<ItemStack> enderresult = config.getList("contents").stream()
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

        return new PersonaInventory(contents, enderContents);
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
