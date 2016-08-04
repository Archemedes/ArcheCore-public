package net.lordofthecraft.arche;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.archemedes.customitem.Customizer;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.CasketTask;
import org.apache.commons.lang.RandomStringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * A treasure chest is an item that players can click to pry open. 
 * In doing so, they are randomly awarded 1 or 2 items from the predefined loot tables
 * Treasure chests are used as a convenient high-end reward for skills.
 */
public class TreasureChest {

	private static Map<ItemStack, Integer> loot;
	private static File file;

	private TreasureChest() {
	}

	/**
	 * Used by ArcheCore. Do not call.
	 * @param plugin Plugin to initialize for.
	 */
	public static void init(Plugin plugin){

		file = new File(plugin.getDataFolder(), "chestloot.yml");
		FileConfiguration c = YamlConfiguration.loadConfiguration(file);

		loot = Maps.newHashMap();

		if(c.isConfigurationSection("index")){
			for (Entry<String, Object> entry : c.getConfigurationSection("index").getValues(false).entrySet()) {
				Integer in = (Integer) entry.getValue();
				//Customizer.deserialize(entry.);
				ItemStack item = c.getItemStack(entry.getKey());
				if (item.getType() != Material.EMPTY_MAP)
					loot.put(item, in);
				else {
					String tag = "nexusrecipe#"+item.getItemMeta().getDisplayName();
					loot.put(Customizer.giveCustomTag(item, tag), in);
				}
			}
		}
	}

	public static void initSQL() {
		Map<String, String> cols = Maps.newLinkedHashMap();
		cols.put("time", "REAL NOT NULL");
		cols.put("player", "TEXT NOT NULL");
		cols.put("id", "INT NOT NULL");
		cols.put("luck", "REAL");
		cols.put("rewards", "TEXT NOT NULL");
		cols.put("UNIQUE (time,player,id,rewards)", "ON CONFLICT REPLACE");
		ArcheCore.getControls().getSQLHandler().createTable("casket_log", cols);
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
		ItemStack[] items = rollItems(ArcheCore.getControls().getPersonaHandler().getLuck(p));
		if(items == null) return;
		SaveHandler.getInstance().put(new CasketTask(ArcheCore.getControls().getPersonaHandler().getPersona(p), ArcheCore.getControls().getPersonaHandler().getLuck(p), items));
		//Give items to player, drop what's left
		for(ItemStack leftover : p.getInventory().addItem(items).values())
			p.getWorld().dropItem(p.getLocation(), leftover);
	}
	
	/**
	 * Generate treasure items randomly as part of the loot gained from a Treasure Chest
	 * @param luck The luck value to modify the loot by, usually based off of player luck attribute
	 * @return An array containing all the items randomly picked from the TreasureChest drop table
	 */
	public static ItemStack[] rollItems(Double luck) {
		//501 edit - added luck
		luck = Math.min(luck, 5);
		luck = Math.max(luck, -5);
		//Capping luck to avoid badness
		if(loot.isEmpty()) return null;

		int t = 0;
		for(int i : loot.values()) t+=i;

		Random rnd = new Random();

		int times = rnd.nextDouble() < 0.3 + (0.1 * luck) ? 2 : 1;
		ItemStack[] items = new ItemStack[times];

		//Pick items from drop table
		for(int i=0; i < times; i++){
			int roll = rnd.nextInt(t);
			roll += luck * 10;
			roll = Math.max(roll, 0); //avoiding negatives which could cause issues and increase the drop chances
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

	private static List<ItemStack> getItemList(){
		Set<ItemStack> keys = TreasureChest.loot.keySet();
		Iterator<ItemStack> it= keys.iterator();
		if(keys.size() > 0) {
			List<ItemStack> items = new ArrayList<>();
			for (int i = 0; i < keys.size() && it.hasNext(); i++) {
				items.add(it.next());
			}
			return items;
		}
		return null;
	}

	public static List<ItemStack> first54(){
		final List<ItemStack> items = getItemList();
		if(items != null) {
			List<ItemStack> first53Items = new ArrayList<>(54);
			for (int i = 0; i < 55; i++) {
				first53Items.add(items.get(i));
			}
			return first53Items;
		}else{
			return null;
		}
	}

	public static List<ItemStack> remainingItems(){
		final List<ItemStack> items = getItemList();
		if(items != null) {
			List<ItemStack> remains = new ArrayList<>();
			for (int i = 54; i < items.size(); i++) {
				remains.add(items.get(i));
			}
			return remains;
		}else{
			return null;
		}
	}
}
