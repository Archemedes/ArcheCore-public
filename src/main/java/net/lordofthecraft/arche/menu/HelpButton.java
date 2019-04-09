package net.lordofthecraft.arche.menu;

import static net.md_5.bungee.api.ChatColor.*;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.util.ItemUtil;
import co.lotc.core.bukkit.util.Run;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.help.HelpDesk;

public class HelpButton extends Button {

	@Override
	public ItemStack getItemStack(MenuAgent agent) {
		ItemStack is = new ItemStack(Material.BOOK);
		ItemUtil.decorate(is, YELLOW + "Help", WHITE + "Receive help on", WHITE + "various topics.");
		return is;
	}

	@Override
	public void click(MenuAction action) {
		action.getMenuAgent().close();
		Run.as(ArcheCore.getPlugin()).delayed(2, ()->HelpDesk.getInstance().openHelpMenu(action.getPlayer()));
	}

}
