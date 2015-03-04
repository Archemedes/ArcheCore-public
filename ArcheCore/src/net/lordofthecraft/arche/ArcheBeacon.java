package net.lordofthecraft.arche;

import org.bukkit.entity.*;
import org.bukkit.*;
import java.util.logging.*;
import net.lordofthecraft.arche.persona.*;
import org.bukkit.inventory.*;
import com.google.common.collect.*;
import java.util.*;
import org.bukkit.inventory.meta.*;

public class ArcheBeacon
{
    public static final String BEACON_HEADER;
    
    public static void openBeacon(final Player p) {
        if (!CreationDialog.mayConverse(p)) {
            return;
        }
        final Logger log = ArcheCore.getPlugin().getLogger();
        final ArchePersonaHandler handler = ArchePersonaHandler.getInstance();
        if (handler.mayUse(p) || (handler.countPersonas(p) == 0 && !p.hasPermission("archecore.exempt"))) {
            final ArchePersona[] prs = handler.getAllPersonas((OfflinePlayer)p);
            if (prs == null) {
                log.severe(" [Beacon] Player walking around without registered Personas File!");
                return;
            }
            int count = 0;
            int current = -1;
            for (int i = 0; i < prs.length; ++i) {
                if (prs[i] != null) {
                    ++count;
                    if (prs[i].isCurrent()) {
                        current = i;
                    }
                }
            }
            final Inventory inv = Bukkit.createInventory((InventoryHolder)p, 9, ArcheBeacon.BEACON_HEADER);
            final String r = ChatColor.RESET.toString();
            final String g = ChatColor.DARK_GRAY.toString();
            if (current < 0) {
                if (count == 0) {
                    log.warning("[Beacon] Zero personas for: " + p.getName());
                }
                else {
                    log.warning("[Beacon] no current persona for: " + p.getName());
                }
            }
            else {
                final boolean xp = prs[current].getXPGain();
                ItemStack is = new ItemStack(xp ? Material.EYE_OF_ENDER : Material.ENDER_PEARL);
                final String xpGain = xp ? (ChatColor.GREEN + "" + ChatColor.ITALIC + "ON") : (ChatColor.DARK_RED + "" + ChatColor.ITALIC + "OFF");
                buildItem(is, r + "XP gain: " + xpGain, g + "Click to toggle");
                inv.setItem(0, is);
                is = new ItemStack(Material.ENCHANTED_BOOK);
                buildItem(is, r + "Read Skill Tomes", g + "Consume all skill tomes", g + "in your Persona's inventory.");
                inv.setItem(3, is);
            }
            ItemStack is = new ItemStack(Material.BOOK);
            buildItem(is, r + "Help", g + "Receive help on", g + "various topics.");
            inv.setItem(1, is);
            is = new ItemStack(Material.ENDER_CHEST);
            buildItem(is, r + "Ender Chest", g + "Open your Ender Chest");
            inv.setItem(2, is);
            final int max = handler.getAllowedPersonas(p);
            is = new ItemStack(Material.REDSTONE_COMPARATOR);
            buildItem(is, r + "Your Personas to the right", ChatColor.GRAY + "Max Personas: " + ChatColor.LIGHT_PURPLE + max, g + "Left Click to select", g + "SHIFT + Left Click: Create new", g + "SHIFT + Right Click: Permakill Persona", ChatColor.GRAY + "Click me for more info.");
            inv.setItem(4, is);
            final boolean mayMakeMore = count < max;
            for (int j = 0; j < 4; ++j) {
                final ArchePersona a = prs[j];
                if (a == null) {
                    is = new ItemStack(Material.SKULL_ITEM, 1, (short)(mayMakeMore ? 0 : 1));
                    if (mayMakeMore) {
                        buildItem(is, "Empty Persona", ChatColor.GREEN + "" + ChatColor.ITALIC + "Click here", g + "To create a new Persona");
                    }
                    else {
                        buildItem(is, "Locked Slot", g + "Please " + ChatColor.GREEN + "" + ChatColor.ITALIC + "Donate", g + "To be able to make", "more Personas");
                    }
                }
                else {
                    is = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
                    final String name = ChatColor.YELLOW + "" + ChatColor.ITALIC + a.getName();
                    final String gender = (a.getGender() == null) ? "" : a.getGender();
                    final String desc = ChatColor.GRAY + a.getRaceString() + " " + gender + ", " + a.getAge();
                    final String d2 = (j == current) ? (ChatColor.DARK_GREEN + "Selected!") : (ChatColor.GREEN + "Click to select");
                    buildItem(is, name, desc, d2);
                }
                inv.setItem(5 + j, is);
            }
            p.openInventory(inv);
        }
        else {
            p.sendMessage(ChatColor.RED + "You may not yet access this.");
        }
    }
    
    private static ItemStack buildItem(final ItemStack is, final String title, final String... lore) {
        final ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(title);
        final LinkedList<String> loreList = Lists.newLinkedList();
        for (final String x : lore) {
            loreList.add(ChatColor.DARK_GRAY + x);
        }
        meta.setLore((List)loreList);
        is.setItemMeta(meta);
        return is;
    }
    
    static {
        BEACON_HEADER = ChatColor.AQUA + "" + ChatColor.BOLD + "Your settings:";
    }
}
