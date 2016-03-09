package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.TreasureChest;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandTreasurechest implements CommandExecutor {

	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String b = ChatColor.BLUE + "";
		String r = ChatColor.RESET+ "";
		
		if(args.length == 0 || args[0].equalsIgnoreCase("help")){
			sender.sendMessage(b + "The treasure table holds " + r + TreasureChest.getLootCount() + " items.");
			sender.sendMessage("/tc add [frequency]: " + b + "add item in your hand to the loot tables." );
			sender.sendMessage("/tc give: " + b + " Give a treasure chest." );
			return true;
		} else if (sender instanceof Player){
			Player p = (Player) sender;
			
			if(args[0].equalsIgnoreCase("give")){
				p.getInventory().addItem(TreasureChest.giveChest());
				p.sendMessage(b + "Gave you a treasure chest. Enjoy!");
				return true;
			} else if (args[0].equalsIgnoreCase("add") && args.length >= 2 && StringUtils.isNumeric(args[1])){
				ItemStack is = p.getEquipment().getItemInMainHand();
				if(is.getType() == Material.AIR){
					p.sendMessage(ChatColor.RED + "Not holding an itemstack.");
				} else {
					int freq = Integer.parseInt(args[1]);
					TreasureChest.addItem(is, freq);
					p.sendMessage(b + "Added the item in your hand to loot table!");
				}
				return true;
			}
		}
		
		return false;
	}

}
