package net.lordofthecraft.arche.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.util.ItemUtil;
import lombok.var;
import net.lordofthecraft.arche.command.CommandTemplate;
import net.lordofthecraft.arche.command.annotate.Cmd;

public class CommandDurability extends CommandTemplate {
	
	@Cmd("set a durability tag")
	public void set(Player p, int i) {
		ItemStack is = hand(p);
		
		 if(i <= -100) i = -99;
		 boostDurability(is, i);
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
	
	public static ItemStack boostDurability(ItemStack is, int percentage) {
		if(percentage <= -100) throw new IllegalArgumentException("Cannot have -100% durability");
		ItemUtil.setCustomTag(is, "dura_boost", Integer.toString(percentage));
		var meta = is.getItemMeta();
		List<String> lore = meta.hasLore()? meta.getLore() : new ArrayList<>();
		
		ChatColor c = percentage < 0? ChatColor.RED : ChatColor.BLUE;
		char plus = percentage < 0? '-' : '+';
		String percent = Integer.toString(Math.abs(percentage));
		
		lore.add(c.toString() + plus + percent + "% Durability");
		meta.setLore(lore);
		is.setItemMeta(meta);
		return is;
	}
}
