package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.TreasureChest;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CommandTreasurechest implements CommandExecutor {

	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String b = ChatColor.BLUE + "";
		String r = ChatColor.RESET+ "";
		
		if(args.length == 0 || args[0].equalsIgnoreCase("help")){
			sender.sendMessage(b + "The treasure table holds " + r + TreasureChest.getLootCount() + " items.");
			sender.sendMessage("/trch add [frequency]: " + b + "add item in your hand to the loot tables." );
			sender.sendMessage("/trch give: " + b + " Give a treasure chest." );
			sender.sendMessage("/trch view [1/2]:" + b + " View specified page of the casket loot table.");
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
			}else if(args.length == 2 && args[0].equalsIgnoreCase("view") && StringUtils.isNumeric(args[1])){
				//add inventory click event check inv title if equal to title of inv if so cancel e so ppl cant drag items out
				if(args[1].equalsIgnoreCase("1")){
					final Inventory inv = Bukkit.createInventory(null,54,ChatColor.GOLD + "Casket Table(Pg.1)");
					try {
						for (ItemStack i : TreasureChest.first54()) {
							inv.addItem(i);
						}
						p.sendMessage("Opening pg. 1 of casket table..");
						p.openInventory(inv);
					}catch (Exception e){
						p.sendMessage(ChatColor.RED + "Empty set!");
					}

				}else if(args[1].equalsIgnoreCase("2")){
					final Inventory inv = Bukkit.createInventory(null,54,ChatColor.GOLD + "Casket Table(Pg.2)");
					try {
						for (ItemStack i : TreasureChest.remainingItems()) {
							inv.addItem(i);
						}
						p.sendMessage("Opening pg. 2 of casket table..");
						p.openInventory(inv);
					}catch (Exception e){
						p.sendMessage(ChatColor.RED + "Empty set!");
					}

				}else{
					p.sendMessage(ChatColor.RED + "Not a valid page!");
				}
			}
		}
		
		return false;
	}

}
