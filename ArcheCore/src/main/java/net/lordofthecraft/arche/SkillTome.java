package net.lordofthecraft.arche;

import java.util.LinkedHashMap;
import java.util.List;

import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.enums.SkillTier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;

import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SkillTome {

	public static void init(SQLHandler handle) {
		if (ArcheCore.getControls().getSQLHandler() == handle) {
			LinkedHashMap<String,String> cols = Maps.newLinkedHashMap();
			cols.put("time", "INT");
			cols.put("player", "TEXT");
			cols.put("id", "INT");
			cols.put("xp", "REAL");
			cols.put("skill", "TEXT");
			handle.createTable("sktome_log", cols);
		}
	}
	
	public static double getXpBoost(double currentXp){
		//Find amount of XP below the bungling tier. Save from infinite Rusty
		double defecit = SkillTier.BUNGLING.getXp() - currentXp;
		
		//Find tier of Player
		SkillTier result = SkillTier.RUSTY;
		for(SkillTier st : SkillTier.values()){
			if(st.getXp() <= currentXp) result = st;
			else break;
		}
		int x = result.getTier();
		double boost = 1250 + 250*x + 5*(Math.pow(x, 3));
		
		return Math.max(defecit, boost);
	}
	
	/**
	 * Return the Item that functions as an XP tome for the given Skill
	 * @param skill the Skill for which to create the tome
	 * @return The skill tome
	 */
	public static ItemStack giveTome(Skill skill){
		ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
		ItemMeta meta = result.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "Tome of the " + WordUtils.capitalize(skill.getName()));
		List<String> lore = Lists.newArrayList();
		lore.add(ChatColor.DARK_GRAY + "Brimming with raw knowledge.");
		meta.setLore(lore);
		result.setItemMeta(meta);
		return result;
	}
	
	/**
	 * Consume all tomes a player holds in their inventory, and award the player with XP
	 * @param p The player to process
	 */
	public static void consumeTomes(Player p){
		
		
		ArchePersona pers = ArchePersonaHandler.getInstance().getPersona(p);
		if(pers == null){ 
			p.sendMessage(ChatColor.RED + "You are currently not attuned to a Persona.");
		} else {
			p.sendMessage(ChatColor.GOLD + "You absorb all Tomes in your inventory.");
			boolean hasTome = false;
			p.playSound(p.getLocation(), Sound.ORB_PICKUP, 2f, 1f);
			
			int i = 0;
			for(ItemStack is : p.getInventory()){
				
				if(is != null){
					if(is.getType() == Material.ENCHANTED_BOOK && is.hasItemMeta()){
						
						ItemMeta meta = is.getItemMeta();
						if(meta.hasDisplayName() && meta.hasLore()){
						
							String name = meta.getDisplayName();
							if(name.startsWith(ChatColor.GOLD + "Tome of the") &&
									meta.getLore().get(0).equals(ChatColor.DARK_GRAY + "Brimming with raw knowledge.")){
								
								int amt = is.getAmount();
								is.setType(Material.AIR);
								p.getInventory().setItem(i, is);
								
								String sname = name.substring(14);
								
								Skill skill = ArcheSkillFactory.getSkill(sname);
								if(skill != null){ 
									double xp = amt*getXpBoost(skill.getXp(pers));
									skill.addRawXp(pers, xp);
									hasTome = true;
									log(skill, pers, xp);
								}
							}
						}
					}
				}
				//Increment inventory slot
				i++;
			}
			
			if(!hasTome){
				p.sendMessage(ChatColor.LIGHT_PURPLE + "You have no experience tomes on your person.");
			}
		}
	}
	
	/*
	 * 			cols.put("time", "INT");
			cols.put("player", "TEXT");
			cols.put("id", "INT");
			cols.put("amount", "REAL");
			cols.put("skill", "TEXT");
	 */
	
	private static void log(Skill skill, Persona pers, double xp) {
		LinkedHashMap<String,Object> vals = Maps.newLinkedHashMap();
		vals.put("time", System.currentTimeMillis());
		vals.put("player", pers.getPlayerUUID().toString());
		vals.put("id", pers.getId());
		vals.put("xp", xp);
		vals.put("skill", skill.getName());
		
		ArcheCore.getControls().getSQLHandler().insert("sktome_log", vals);
	}
	
/*	private static void deduct(ItemStack is){
		if(is.getAmount() == 1)
			is.setType(Material.AIR);
		else
			is.setAmount(is.getAmount() - 1);
	}*/
	
	private SkillTome(){}
}
