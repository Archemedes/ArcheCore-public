package net.lordofthecraft.arche.commands;

import org.bukkit.command.*;
import net.lordofthecraft.arche.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.apache.commons.lang.*;
import org.bukkit.*;

public class CommandTreasurechest implements CommandExecutor
{
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final String b = ChatColor.BLUE + "";
        final String r = ChatColor.RESET + "";
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(b + "The treasure table holds " + r + TreasureChest.getLootCount() + " items.");
            sender.sendMessage("/tc add [frequency]: " + b + "add item in your hand to the loot tables.");
            sender.sendMessage("/tc give: " + b + " Give a treasure chest.");
            return true;
        }
        if (sender instanceof Player) {
            final Player p = (Player)sender;
            if (args[0].equalsIgnoreCase("give")) {
                p.getInventory().addItem(new ItemStack[] { TreasureChest.giveChest() });
                p.sendMessage(b + "Gave you a treasure chest. Enjoy!");
                return true;
            }
            if (args[0].equalsIgnoreCase("add") && args.length >= 2 && StringUtils.isNumeric(args[1])) {
                final ItemStack is = p.getItemInHand();
                if (is.getType() == Material.AIR) {
                    p.sendMessage(ChatColor.RED + "Not holding an itemstack.");
                }
                else {
                    final int freq = Integer.parseInt(args[1]);
                    TreasureChest.addItem(is, freq);
                    p.sendMessage(b + "Added the item in your hand to loot table!");
                }
                return true;
            }
        }
        return false;
    }
}
