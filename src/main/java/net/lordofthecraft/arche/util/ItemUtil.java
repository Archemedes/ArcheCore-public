package net.lordofthecraft.arche.util;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.utility.MinecraftReflection;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TranslatableComponent;

import static net.lordofthecraft.arche.util.ReflectionUtil.*;

public class ItemUtil {
	
	/**
	 * Transforms an itemstack into its JSON equivalent. Useful for HoverEvent SHOW_ITEM or uncommon serialization needs
	 * @param is The item to convert
	 * @return A JSON string (can be turned to JSON object if desired)
	 */
	public static String getItemJson(ItemStack is) {
		try {
			Object nms = MinecraftReflection.getMinecraftItemStack(is);
			Object compound = compoundConstructor().newInstance();
			
			saveToJson().invoke(nms, compound);
			return compound.toString();
		}catch(Throwable t){t.printStackTrace();}
		return null;
	}
	
	/**
	 * Name of ItemStack as displayed by en_US language
	 * @param is ItemStack to check
	 * @return the translated name
	 */
	public static String getItemEnglishName(ItemStack is){
		TranslatableComponent comp = new TranslatableComponent();
		comp.setTranslate(getItemLocaleName(is));
		return comp.toPlainText();
	}
	
	/**
	 * Returns best-fitting name of ItemStack.
	 * Either the display name from Item Meta, or else the en_US english name of the item
	 * @param is ItemStack to check
	 * @return the display name
	 */
	public static String getDisplayName(ItemStack is){
		if(is.hasItemMeta()) {
			ItemMeta m = is.getItemMeta();
			if(m.hasDisplayName()) return m.getDisplayName();
		}
		
		return getItemEnglishName(is);
	}

	/**
	 * Retrieve item name recognized by MCs Locale translations
	 * @param is Item to check
	 * @return The internal ItemStack translatable name
	 */
	public static String getItemLocaleName(ItemStack is){
		try{
			Object nmsItemStack =  MinecraftReflection.getMinecraftItemStack(is);
			return itemNameMethod().invoke(nmsItemStack).toString() + ".name";
		}catch(Throwable t){t.printStackTrace();}
		return null;
	}
	
	/**
	 * Convenience method for checking if an ItemStack is non-null and non-AIR
	 * @param is ItemStack to check
	 * @return is != null && is.getType() != Material.AIR;
	 */
	public static boolean exists(ItemStack is) {
		return is != null && is.getType() != Material.AIR;
	}
	
	/**
	 * Conveniently make a new item.
	 * @param mat Material of button
	 * @param displayName Name (not italicized by default)
	 * @param lore Lore lines
	 * @return the Item
	 */
	public static ItemStack make(Material mat, String displayName, String... lore) {
		ItemStack is = new ItemStack(mat);
		ItemMeta m = is.getItemMeta();
		if(displayName.charAt(0) != ChatColor.COLOR_CHAR) displayName = ChatColor.WHITE + displayName;
		m.setLore(Arrays.asList(lore));
		is.setItemMeta(m);
		return is;
	}
}
