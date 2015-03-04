package net.lordofthecraft.arche;

import org.bukkit.inventory.*;
import org.bukkit.plugin.*;
import org.bukkit.configuration.file.*;
import org.bukkit.*;

import com.google.common.collect.*;

import org.bukkit.inventory.meta.*;
import org.apache.commons.lang.*;

import java.io.*;

import org.bukkit.entity.*;

import java.util.*;

public class TreasureChest
{
    private static Map<ItemStack, Integer> loot;
    private static File file;
    
    public static void init(final Plugin plugin) {
        TreasureChest.file = new File(plugin.getDataFolder(), "chestloot.yml");
        final FileConfiguration c = (FileConfiguration)YamlConfiguration.loadConfiguration(TreasureChest.file);
        TreasureChest.loot = Maps.newHashMap();
        if (c.isConfigurationSection("index")) {
            for (final Map.Entry<String, Object> entry : c.getConfigurationSection("index").getValues(false).entrySet()) {
                final Integer in = (Integer) entry.getValue();
                TreasureChest.loot.put(c.getItemStack((String)entry.getKey()), in);
            }
        }
    }
    
    public static int getLootCount() {
        return TreasureChest.loot.size();
    }
    
    public static ItemStack giveChest() {
        final ItemStack result = new ItemStack(Material.CHEST);
        final ItemMeta meta = result.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Casket");
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.DARK_GRAY + "What boons lie within?");
        meta.setLore((List)lore);
        result.setItemMeta(meta);
        return result;
    }
    
    public static void addItem(final ItemStack i, final int j) {
        TreasureChest.loot.put(i, j);
        final FileConfiguration c = (FileConfiguration)YamlConfiguration.loadConfiguration(TreasureChest.file);
        final String path = RandomStringUtils.randomAlphanumeric(10);
        if (!c.isConfigurationSection("index")) {
            c.createSection("index");
        }
        c.getConfigurationSection("index").set(path, (Object)j);
        c.set(path, (Object)i);
        try {
            c.save(TreasureChest.file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void giveLoot(final Player p) {
        final ItemStack[] items = rollItems();
        if (items == null) {
            return;
        }
        for (final ItemStack leftover : p.getInventory().addItem(items).values()) {
            p.getWorld().dropItem(p.getLocation(), leftover);
        }
    }
    
    public static ItemStack[] rollItems() {
        if (TreasureChest.loot.isEmpty()) {
            return null;
        }
        int t = 0;
        for (final int i : TreasureChest.loot.values()) {
            t += i;
        }
        final Random rnd = new Random();
        final int times = (rnd.nextDouble() < 0.3) ? 2 : 1;
        final ItemStack[] items = new ItemStack[times];
        for (int j = 0; j < times; ++j) {
            int roll = rnd.nextInt(t);
            for (final Map.Entry<ItemStack, Integer> entry : TreasureChest.loot.entrySet()) {
                roll -= entry.getValue();
                if (roll < 0) {
                    items[j] = new ItemStack((ItemStack)entry.getKey());
                    break;
                }
            }
        }
        return items;
    }
}
