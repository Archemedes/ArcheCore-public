package net.lordofthecraft.arche;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.enums.SkillTier;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.StatementTask;
import net.lordofthecraft.arche.skill.ArcheSkillFactory;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

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
			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 2f, 1f);
			
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
									double newxp = Math.round(skill.addRawXp(pers, xp, true));
									hasTome = true;
                                    SaveHandler.getInstance().put(new SkillTomeStatementTask(skill, pers, newxp));
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
	
/*	private static void deduct(ItemStack is){
		if(is.getAmount() == 1)
			is.setType(Material.AIR);
		else
			is.setAmount(is.getAmount() - 1);
	}*/
	
	private SkillTome(){}

	public static class SkillTomeStatementTask extends StatementTask {

        private final long time;
        private final Persona pers;
        private final double amt;
        private final Skill skill;

		public SkillTomeStatementTask(Skill skill, Persona pers, double xp) {
            time = System.currentTimeMillis();
            this.pers = pers;
            this.amt = xp;
            this.skill = skill;
		}

		@Override
		protected void setValues() throws SQLException {
            stat.setLong(1, time);
            stat.setString(2, pers.getPlayerUUID().toString());
            stat.setInt(3, pers.getId());
            stat.setDouble(4, amt);
            stat.setString(5, skill.getName());
		}

		@Override
		protected String getQuery() {
			return "INSERT INTO sktome_log VALUES (?,?,?,?,?)";
		}

	}
}
