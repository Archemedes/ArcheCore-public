package net.lordofthecraft.arche.menu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.util.ItemUtil;

public class AvailablePersonaButton extends Button {

	@Override
	public void click(MenuAction ma) {
		
	}

	@Override
	public ItemStack getItemStack(MenuAgent ma) {
		ItemStack is = new ItemStack(Material.ZOMBIE_HEAD, 1);
		return ItemUtil.decorate(is, "Empty Persona", ChatColor.GREEN+""+ChatColor.ITALIC + "Slot is available");
	}

}
