package net.lordofthecraft.arche.util;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtil {

	public static int countInventory(Inventory inv, ItemStack is) {
		int count = 0;
		for(ItemStack other : inv.getContents()) {
			if(other == null) continue;
			if(other.isSimilar(is)) count += other.getAmount();
		}
		return count;
	}
	
	public static boolean isEmpty(Inventory inv) {
		for(ItemStack is : inv.getContents()) {
			if(is != null) return false;
		}
		return true;
	}
}
