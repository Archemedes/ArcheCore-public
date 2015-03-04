package net.lordofthecraft.arche.help;

import com.google.common.collect.*;
import org.bukkit.entity.*;
import net.lordofthecraft.arche.*;
import org.bukkit.plugin.*;
import org.bukkit.*;
import org.bukkit.inventory.*;
import java.util.*;
import org.apache.commons.lang.*;

public class HelpDesk
{
    public static final String HELP_HEADER;
    private final Map<String, HelpFile> normalTopics;
    private final Map<String, HelpFile> skillTopics;
    private final Map<String, HelpFile> infoTopics;
    
    public HelpDesk() {
        super();
        this.normalTopics = Maps.newLinkedHashMap();
        this.skillTopics = Maps.newHashMap();
        this.infoTopics = Maps.newHashMap();
    }
    
    public Set<String> getTopics() {
        return Collections.unmodifiableSet((Set<? extends String>)this.normalTopics.keySet());
    }
    
    public void addTopic(final HelpFile help) {
        this.normalTopics.put(help.getTopic().toLowerCase(), help);
    }
    
    public void addTopic(String topic, final String output) {
        topic = topic.toLowerCase();
        final HelpFile help = this.constructHelp(topic, output);
        this.normalTopics.put(topic, help);
    }
    
    public void addTopic(String topic, final String output, final Material icon) {
        topic = topic.toLowerCase();
        final HelpFile help = this.constructHelp(topic, output);
        help.setIcon(icon);
        this.normalTopics.put(topic, help);
    }
    
    public void addInfoTopic(final HelpFile help) {
        this.infoTopics.put(help.getTopic().toLowerCase(), help);
    }
    
    public void addInfoTopic(String topic, final String output) {
        topic = topic.toLowerCase();
        final HelpFile help = this.constructHelp(topic, output);
        this.infoTopics.put(topic, help);
    }
    
    public void addSkillTopic(final HelpFile help) {
        this.skillTopics.put(help.getTopic().toLowerCase(), help);
    }
    
    public void addSkillTopic(String topic, final String output) {
        topic = topic.toLowerCase();
        final HelpFile help = this.constructHelp(topic, output);
        this.skillTopics.put(topic, help);
    }
    
    public void addSkillTopic(String topic, final String output, final Material icon) {
        topic = topic.toLowerCase();
        final HelpFile help = this.constructHelp(topic, output);
        help.setIcon(icon);
        this.skillTopics.put(topic, help);
    }
    
    private HelpFile constructHelp(final String topic, final String output) {
        return new LinkedHelpFile(topic, output);
    }
    
    public void outputHelp(String topic, final Player p) {
        topic = topic.toLowerCase();
        final HelpFile h = this.findHelpFile(topic);
        if (h == null) {
            if (ArcheCore.getPlugin().getWikiUsage()) {
                p.sendMessage(ChatColor.LIGHT_PURPLE + "Fetching help file, please wait...");
                final WikiHelpFile wh = new WikiHelpFile(topic);
                new WikiHelpFile.WikiBrowser(p, wh).runTaskAsynchronously((Plugin)ArcheCore.getPlugin());
            }
            else {
                p.sendMessage(ChatColor.RED + "No such help topic: " + ChatColor.GRAY + topic);
            }
        }
        else {
            h.output(p);
        }
    }
    
    public void outputSkillHelp(String topic, final Player p) {
        topic = topic.toLowerCase();
        final HelpFile h = this.skillTopics.get(topic);
        if (h == null) {
            p.sendMessage(ChatColor.RED + "No such skill: " + ChatColor.GRAY + topic);
        }
        else {
            h.output(p);
        }
    }
    
    public String getHelpText(String topic) {
        topic = topic.toLowerCase();
        final HelpFile h = this.findHelpFile(topic);
        if (h == null) {
            return null;
        }
        return h.outputHelp();
    }
    
    public String getSkillHelpText(String topic) {
        topic = topic.toLowerCase();
        final HelpFile h = this.skillTopics.get(topic);
        if (h == null) {
            return null;
        }
        return h.outputHelp();
    }
    
    public void openHelpMenu(final Player p) {
        final int sizeone = this.round(this.normalTopics.size(), 9) + 9;
        final int sizetwo = this.round(this.skillTopics.size(), 9);
        final boolean large = sizeone + sizetwo > 54;
        final int size = large ? 54 : (sizeone + sizetwo);
        if (size == 0) {
            return;
        }
        final Inventory inv = Bukkit.createInventory((InventoryHolder)p, size, HelpDesk.HELP_HEADER);
        int i = 0;
        for (final HelpFile h : this.normalTopics.values()) {
            inv.setItem(i++, h.asItem());
        }
        if (!large) {
            i = sizeone;
        }
        for (final HelpFile h : this.skillTopics.values()) {
            inv.setItem(i++, h.asSkillItem());
        }
        p.openInventory(inv);
    }
    
    public HelpFile findHelpFile(final String topic) {
        HelpFile h = this.normalTopics.get(topic);
        if (h == null) {
            h = this.infoTopics.get(topic);
        }
        while (h != null && h.outputHelp().startsWith("@") && h.outputHelp().endsWith("@") && StringUtils.countMatches(h.outputHelp(), "@") == 2) {
            final String referTopic = h.outputHelp().substring(1, h.outputHelp().length() - 2);
            h = this.normalTopics.get(referTopic);
            if (h == null) {
                h = this.infoTopics.get(referTopic);
            }
        }
        return h;
    }
    
    private int round(final double i, final int v) {
        return (int)(Math.ceil(i / v) * v);
    }
    
    public static HelpDesk getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    static {
        HELP_HEADER = ChatColor.DARK_AQUA + "Please choose a Topic";
    }
    
    private static class SingletonHolder
    {
        private static final HelpDesk INSTANCE;
        
        static {
            INSTANCE = new HelpDesk();
        }
    }
}
