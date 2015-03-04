package net.lordofthecraft.arche;

import net.lordofthecraft.arche.enums.*;
import org.bukkit.inventory.*;
import org.apache.commons.lang.*;
import com.google.common.collect.*;
import org.bukkit.inventory.meta.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import net.lordofthecraft.arche.skill.*;
import net.lordofthecraft.arche.interfaces.*;
import net.lordofthecraft.arche.persona.*;
import java.util.*;

public class SkillTome
{
    public static double getXpBoost(final double currentXp) {
        final double defecit = SkillTier.BUNGLING.getXp() - currentXp;
        SkillTier result = SkillTier.RUSTY;
        for (final SkillTier st : SkillTier.values()) {
            if (st.getXp() > currentXp) {
                break;
            }
            result = st;
        }
        final int x = result.getTier();
        final double boost = 1250 + 250 * x + 5.0 * Math.pow(x, 3.0);
        return Math.max(defecit, boost);
    }
    
    public static ItemStack giveTome(final Skill skill) {
        final ItemStack result = new ItemStack(Material.ENCHANTED_BOOK);
        final ItemMeta meta = result.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Tome of the " + WordUtils.capitalize(skill.getName()));
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.DARK_GRAY + "Brimming with raw knowledge.");
        meta.setLore((List)lore);
        result.setItemMeta(meta);
        return result;
    }
    
    public static void consumeTomes(final Player p) {
        final ArchePersona pers = ArchePersonaHandler.getInstance().getPersona(p);
        if (pers == null) {
            p.sendMessage(ChatColor.RED + "You are currently not attuned to a Persona.");
        }
        else {
            p.sendMessage(ChatColor.GOLD + "You absorb all Tomes in your inventory.");
            boolean hasTome = false;
            p.playSound(p.getLocation(), Sound.ORB_PICKUP, 2.0f, 1.0f);
            int i = 0;
            for (final ItemStack is : p.getInventory()) {
                if (is != null && is.getType() == Material.ENCHANTED_BOOK && is.hasItemMeta()) {
                    final ItemMeta meta = is.getItemMeta();
                    if (meta.hasDisplayName() && meta.hasLore()) {
                        final String name = meta.getDisplayName();
                        if (name.startsWith(ChatColor.GOLD + "Tome of the") && meta.getLore().get(0).equals(ChatColor.DARK_GRAY + "Brimming with raw knowledge.")) {
                            final int amt = is.getAmount();
                            is.setType(Material.AIR);
                            p.getInventory().setItem(i, is);
                            final String sname = name.substring(14);
                            final Skill skill = ArcheSkillFactory.getSkill(sname);
                            if (skill != null) {
                                final double xp = amt * getXpBoost(skill.getXp(pers));
                                skill.addRawXp(pers, xp);
                                hasTome = true;
                            }
                        }
                    }
                }
                ++i;
            }
            if (!hasTome) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + "You have no experience tomes on your person.");
            }
        }
    }
    
    private static void deduct(final ItemStack is) {
        if (is.getAmount() == 1) {
            is.setType(Material.AIR);
        }
        else {
            is.setAmount(is.getAmount() - 1);
        }
    }
}
