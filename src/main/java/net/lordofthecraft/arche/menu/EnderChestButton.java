package net.lordofthecraft.arche.menu;

import static net.md_5.bungee.api.ChatColor.*;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.bukkit.util.Run;
import net.lordofthecraft.arche.ArcheCore;

public class EnderChestButton extends Button {

	@Override
	public void click(MenuAction ma) {
		Player p = ma.getPlayer();
		ma.getMenuAgent().close();
		
		if(p.hasPermission("archecore.enderchest")) {
			Run.as(ArcheCore.getPlugin()).delayed(2, ()-> p.openInventory(p.getEnderChest()));
		} else {
			p.sendMessage(RED + "You do not have permission for this.");
		}
	}

	@Override
	public ItemStack getItemStack(MenuAgent ma) {
		if(ArcheCore.getControls().showEnderchestInMenu()) {
			ItemStack is = new ItemStack(Material.ENDER_CHEST);
			return ItemUtil.decorate(is, WHITE + "Ender Chest", GRAY + "Open your Ender Chest");
		} else {
			return null;
		}
	}

	
	
	
}
