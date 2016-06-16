package net.lordofthecraft.arche.help;

import com.google.common.collect.Lists;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class HelpFile {
	private final String topic; 
	private Material icon = Material.SIGN;
	
	public HelpFile(String topic){
		this.topic = WordUtils.capitalize(topic);
	}

	public Material getIcon(){
		return icon;
	}

	public void setIcon(Material icon) {
		this.icon = icon;
	}
	
	public ItemStack asItem(){
		ItemStack i = new ItemStack(icon);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + topic);
		i.setItemMeta(meta);
		
		return i;
	}
	
	public ItemStack asSkillItem(){
		ItemStack i = new ItemStack(icon);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + topic);
		List<String> lore = Lists.newArrayList();
		lore.add(ChatColor.DARK_GRAY + "Learn about this Skill.");
		meta.setLore(lore);
		i.setItemMeta(meta);
		
		return i;
	}
	
	public String getTopic(){
		return ChatColor.RESET + topic;
	}
	
	public abstract void output(Player p);

	public abstract String outputHelp();
}
