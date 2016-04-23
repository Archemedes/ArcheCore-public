package net.lordofthecraft.arche.persona;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.attributes.AttributeItem;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class PersonaInventory {
	private final ItemStack[] armor;
	private ItemStack[] contents;

	private PersonaInventory(ItemStack[] armor, ItemStack[] contents) {
		this.armor = armor;
		this.contents = contents;
	}

	private PersonaInventory(ItemStack[] contents) {
		this.contents = contents;
		armor = null;
	}

	@SuppressWarnings("unchecked")
	public static PersonaInventory restore(String str) throws InvalidConfigurationException{
		YamlConfiguration config = new YamlConfiguration();

		config.loadFromString(str);
        if (config.getKeys(false).contains("armor")) {
            if ((config.getList("armor").size() > 0) ? config.getList("armor").get(0) instanceof ItemStack : ((config.getList("contents").size() > 0) && config.getList("contents").get(0) instanceof ItemStack)) { // Load old way without NBT @Deprecated
                List<?> result = config.getList("armor");
				ItemStack[] armor = result.toArray(new ItemStack[result.size()]);
				result = config.getList("contents");
				ItemStack[] contents = result.toArray(new ItemStack[result.size()]);
				return new PersonaInventory(armor, contents);
            }
        } else if (config.getList("contents").size() > 0 && config.getList("contents").get(0) instanceof ItemStack) {
            List<?> result = config.getList("contents");
			ItemStack[] contents = result.toArray(new ItemStack[result.size()]);
			return new PersonaInventory(contents);
        }

        if (config.getKeys(false).contains("armor")) {
            List<ItemStack> armorlist = Lists.newArrayList();
            armorlist.addAll(((List<LinkedHashMap<String, Object>>) config.getList("armor")).stream().map(AttributeItem::deserialize).collect(Collectors.toList()));
            ItemStack[] armor = armorlist.toArray(new ItemStack[armorlist.size()]);

            List<ItemStack> contentslist = Lists.newArrayList();
            contentslist.addAll(((List<LinkedHashMap<String, Object>>) config.getList("contents")).stream().map(AttributeItem::deserialize).collect(Collectors.toList()));
            ItemStack[] contents = contentslist.toArray(new ItemStack[contentslist.size()]);

            return new PersonaInventory(armor, contents);
        } else {
            List<ItemStack> contentslist = Lists.newArrayList();
            contentslist.addAll(((List<LinkedHashMap<String, Object>>) config.getList("contents")).stream().map(AttributeItem::deserialize).collect(Collectors.toList()));
            ItemStack[] contents = contentslist.toArray(new ItemStack[contentslist.size()]);

            return new PersonaInventory(contents);
        }
	}

	public static PersonaInventory store(Player p){
		return new PersonaInventory(p.getInventory().getContents());
	}

	public ItemStack[] getContents(){
		return contents;
	}

	public PersonaInventory setContents(ItemStack[] contents) {
		this.contents = contents;
		return this;
	}

    @Deprecated
	public ItemStack[] getArmorContents(){
		return armor;
	}

	public String getAsString(){
		YamlConfiguration config = new YamlConfiguration();
		/*List<LinkedHashMap<String, Object>> armorlist = Lists.newArrayList();
		for (ItemStack i : armor) {
			armorlist.add(AttributeItem.serialize(i));
		}
		config.set("armor", armorlist);*/
		List<LinkedHashMap<String, Object>> contentslist = Lists.newArrayList();
		for (ItemStack i : contents) {
			contentslist.add(AttributeItem.serialize(i));
		}
		config.set("contents", contentslist);

		return config.saveToString();
	}
}
