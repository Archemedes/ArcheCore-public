package net.lordofthecraft.arche.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.lordofthecraft.arche.command.CommandTemplate;
import net.lordofthecraft.arche.command.annotate.Cmd;
import net.lordofthecraft.arche.util.ItemUtil;

public class CommandDurability extends CommandTemplate {
	
	@Cmd("set a durability tag")
	public void set(Player p, int i) {
		ItemStack is = hand(p);
		
		 if(i <= -100) i = -99;
		 ItemUtil.boostDurability(is, i);
		 p.getInventory().setItemInMainHand(is);
		 msg(ChatColor.AQUA + "Boosted Durability of item in hand.");
	}
	
	@Cmd("clear the durability tag")
	public void clear(Player p) {
		error("Not implemented yet");
	}
	
	private ItemStack hand(Player p) {
		ItemStack is = p.getInventory().getItemInMainHand();
		
		if(!ItemUtil.exists(is) || is.getType().getMaxDurability() == 0) error("Must be holding a damageable item");
		return is;
	}
}
