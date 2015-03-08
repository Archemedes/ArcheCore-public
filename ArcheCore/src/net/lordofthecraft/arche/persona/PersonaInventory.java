package net.lordofthecraft.arche.persona;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PersonaInventory {
	private final ItemStack[] armor;
	private final ItemStack[] contents;
	
	public static PersonaInventory restore(String str) throws InvalidConfigurationException{
		YamlConfiguration config = new YamlConfiguration();
		
		config.loadFromString(str);

		ItemStack[] armor = (ItemStack[])config.getList("armor").toArray(new ItemStack[0]);
		ItemStack[] contents = (ItemStack[])config.getList("contents").toArray(new ItemStack[0]);
		
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
		config.set("armor", armor);
		config.set("contents", contents);
		
		return config.saveToString();
	}
	
}
