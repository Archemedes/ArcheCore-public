package net.lordofthecraft.arche.commands;

import static org.bukkit.ChatColor.*;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.lordofthecraft.arche.command.CommandTemplate;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.util.ChatBuilder;
import net.lordofthecraft.arche.util.ItemUtil;
import net.lordofthecraft.arche.util.LocationUtil;
import net.lordofthecraft.arche.util.MessageUtil;

public class CommandShowItem extends CommandTemplate {

	
	public void invoke(Persona source, Player target) {
		ItemStack is = source.getPlayer().getInventory().getItemInMainHand();
		validate(ItemUtil.exists(is), "You need to hold an item in your hand to show!");
		validate(LocationUtil.isClose(source.getPlayer(), target, 16), "Can only show items to nearby players!");
		ChatBuilder b = MessageUtil.builder(source.getName()).color(GOLD)
				.append(" is showing you ").color(AQUA)
				.append('[' + ItemUtil.getDisplayName(is) + ']').color(WHITE).hoverItem(is);
		
		b.send(target);
		msg("Showing held item to " + target.getName());
	}
}