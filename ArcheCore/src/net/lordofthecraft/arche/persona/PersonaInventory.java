package net.lordofthecraft.arche.persona;

import java.util.LinkedHashMap;
import java.util.List;
import net.lordofthecraft.arche.attributes.AttributeItem;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

public class PersonaInventory {
	private final ItemStack[] armor;
	private final ItemStack[] contents;

	@SuppressWarnings("unchecked")
	public static PersonaInventory restore(String str) throws InvalidConfigurationException{
		YamlConfiguration config = new YamlConfiguration();

		config.loadFromString(str);
		if ((config.getList("armor").size() > 0) ? config.getList("armor").get(0) instanceof ItemStack : ((config.getList("contents").size() > 0) ? config.getList("contents").get(0) instanceof ItemStack : false)) { // Load old way without NBT @Deprecated
			ItemStack[] armor = (ItemStack[]) config.getList("armor").toArray(new ItemStack[0]);
			ItemStack[] contents = (ItemStack[]) config.getList("contents").toArray(new ItemStack[0]);
			return new PersonaInventory(armor, contents);
		}
		List<ItemStack> armorlist = Lists.newArrayList();
		for (LinkedHashMap<String,Object> map : (List<LinkedHashMap<String,Object>>)config.getList("armor")) {
			armorlist.add(AttributeItem.deserialize(map));
		}
		ItemStack[] armor = armorlist.toArray(new ItemStack[0]);
		
		List<ItemStack> contentslist = Lists.newArrayList();
		for (LinkedHashMap<String,Object> map : (List<LinkedHashMap<String,Object>>)config.getList("contents")) {
			contentslist.add(AttributeItem.deserialize(map));
		}
		ItemStack[] contents = contentslist.toArray(new ItemStack[0]);

		return new PersonaInventory(armor, contents);

	}

	public static PersonaInventory store(Player p){
		PersonaInventory result = new PersonaInventory(p.getInventory().getArmorContents(), p.getInventory().getContents());
		return result;
	}

	private PersonaInventory(ItemStack[] armor, ItemStack[] contents){
		this.armor = armor;
		this.contents = contents;
	}

	public ItemStack[] getContents(){
		return contents;
	}

	public ItemStack[] getArmorContents(){
		return armor;
	}

	public String getAsString(){
		YamlConfiguration config = new YamlConfiguration();
		List<LinkedHashMap<String, Object>> armorlist = Lists.newArrayList();
		for (ItemStack i : armor) {
			armorlist.add(AttributeItem.serialize(i));
		}
		config.set("armor", armorlist);
		List<LinkedHashMap<String, Object>> contentslist = Lists.newArrayList();
		for (ItemStack i : contents) {
			contentslist.add(AttributeItem.serialize(i));
		}
		config.set("contents", contentslist);

		return config.saveToString();
	}

}
