package net.lordofthecraft.arche.help;

import org.apache.commons.lang.*;
import org.bukkit.inventory.*;
import org.bukkit.*;
import org.bukkit.inventory.meta.*;
import com.google.common.collect.*;
import java.util.*;
import org.bukkit.entity.*;

public abstract class HelpFile
{
    private final String topic;
    private Material icon;
    
    public HelpFile(final String topic) {
        super();
        this.icon = Material.SIGN;
        this.topic = WordUtils.capitalize(topic);
    }
    
    public void setIcon(final Material icon) {
        this.icon = icon;
    }
    
    public Material getIcon() {
        return this.icon;
    }
    
    public ItemStack asItem() {
        final ItemStack i = new ItemStack(this.icon);
        final ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + this.topic);
        i.setItemMeta(meta);
        return i;
    }
    
    public ItemStack asSkillItem() {
        final ItemStack i = new ItemStack(this.icon);
        final ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + this.topic);
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.DARK_GRAY + "Learn about this Skill.");
        meta.setLore((List)lore);
        i.setItemMeta(meta);
        return i;
    }
    
    public String getTopic() {
        return ChatColor.RESET + this.topic;
    }
    
    public abstract void output(final Player p0);
    
    public abstract String outputHelp();
}
