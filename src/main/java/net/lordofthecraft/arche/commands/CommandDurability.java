package net.lordofthecraft.arche.commands;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.lordofthecraft.arche.util.ItemUtil;

public class CommandDurability implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Can only be ran by players");
			return true;
		}

		if(args.length == 0 || !NumberUtils.isDigits(args[0])) {
			sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Give a durability as integer.");
			return true;
		}
		
		Player p = (Player) sender;
		ItemStack is = p.getInventory().getItemInMainHand();
		
		if(ItemUtil.exists(is) && is.getType().getMaxDurability() > 0) {
			 int i = NumberUtils.toInt(args[0]);
			 if(i <= -100) i = -99;
			 ItemUtil.boostDurability(is, i);
			 p.getInventory().setItemInMainHand(is);
			 p.sendMessage(ChatColor.AQUA + "Boosted Durability of item in hand.");
		} else {
			p.sendMessage(ChatColor.RED + "Error: " + ChatColor.DARK_RED + " Must be holding a damageable item.");
		}
		
		return true;
	}

}
