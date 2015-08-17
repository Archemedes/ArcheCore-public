package net.lordofthecraft.arche;

import io.github.archemedes.customitem.Customizer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * A treasure chest is an item that players can click to pry open. 
 * In doing so, they are randomly awarded 1 or 2 items from the predefined loot tables
 * Treasure chests are used as a convenient high-end reward for skills.
 */
public class TreasureChest {

	private static Map<ItemStack, Integer> loot;
	private static File file;
	
	/**
	 * Used by ArcheCore. Do not call.
	 * @param plugin Plugin to initialize for.
	 */
	public static void init(Plugin plugin){
		
		file = new File(plugin.getDataFolder(), "chestloot.yml");
		FileConfiguration c = YamlConfiguration.loadConfiguration(file);
		
		loot = Maps.newHashMap();
		
		if(c.isConfigurationSection("index")){
			for(Entry<String, Object> entry : ((Map<String, Object>) c.getConfigurationSection("index").getValues(false)).entrySet()){
				Integer in = (Integer) entry.getValue();
				//Customizer.deserialize(entry.);
				ItemStack item = c.getItemStack(entry.getKey());
				if (item.getType() != Material.MAP)
					loot.put(item, in);
				else {
					String tag = "nexusrecipe#"+item.getItemMeta().getDisplayName();
					Customizer.giveCustomTag(item, tag);
					loot.put(item, in);
				}
			}
		}
		
	}
	
	/**
	 * Amount of items in the drop table
	 * @return Amount of items in drop table
	 */
	public static int getLootCount(){
		return loot.size();
	}
	
	/**
	 * Returns the item that functions as a Treasure Chest in the ArcheCore system.
	 * @return a single treasure chest item
	 */
	public static ItemStack giveChest(){
		ItemStack result = new ItemStack(Material.CHEST);
		ItemMeta meta = result.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "Casket");
		List<String> lore = Lists.newArrayList();
		lore.add(ChatColor.DARK_GRAY + "What boons lie within?");
		meta.setLore(lore);
		
		result.setItemMeta(meta);
		return result;
	}
	
	/**
	 * Add an item to the drop tables; calling this method must not be done in repetition.
	 * @param i The ItemStack to add
	 * @param j The relative frequency of this item's occurance in the loot table
	 */
	public static void addItem(ItemStack i, int j){
		loot.put(i, j);
		
		FileConfiguration c = YamlConfiguration.loadConfiguration(file);
		String path = RandomStringUtils.randomAlphanumeric(10);
		
		if(!c.isConfigurationSection("index")) c.createSection("index");
		
		c.getConfigurationSection("index").set(path, j);
		c.set(path, i);
		
		try {c.save(file);} catch (IOException e) {e.printStackTrace();}
	}
	
	/**
	 * Award player with loot as if they just opened a Treasure Chest.
	 * Loot not fitting in the player's inventory will be dropped at their feet.
	 * @param p the player to award
	 */
	public static void giveLoot(Player p){
		ItemStack[] items = rollItems();
		if(items == null) return;
		
		//Give items to player, drop what's left
		for(ItemStack leftover : p.getInventory().addItem(items).values())
			p.getWorld().dropItem(p.getLocation(), leftover);
	}
	
	/**
	 * Generate treasure items randomly as part of the loot gained from a Treasure Chest
	 * @return An array containing all the items randomly picked from the TreasureChest drop table
	 */
	public static ItemStack[] rollItems(){
		if(loot.isEmpty()) return null;
		
		int t = 0;
		for(int i : loot.values()) t+=i;
		
		Random rnd = new Random();
		
		int times = rnd.nextDouble() < 0.3? 2:1;
		ItemStack[] items = new ItemStack[times];
		
		//Pick items from drop table
		for(int i=0; i < times; i++){
			int roll = rnd.nextInt(t);
			for(Entry<ItemStack, Integer> entry : loot.entrySet()){
				roll -= entry.getValue();
				if(roll < 0){
					items[i] = new ItemStack(entry.getKey());
					break;
				}
			}
		}
		
		return items;
	}
	
	private TreasureChest(){}
}
