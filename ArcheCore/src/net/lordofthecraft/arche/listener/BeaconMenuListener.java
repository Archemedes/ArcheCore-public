package net.lordofthecraft.arche.listener;

import java.util.*;
import net.lordofthecraft.arche.help.*;
import com.google.common.collect.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.*;
import org.bukkit.plugin.*;
import net.lordofthecraft.arche.*;
import org.bukkit.*;
import org.bukkit.inventory.*;
import net.lordofthecraft.arche.persona.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;

public class BeaconMenuListener implements Listener
{
    private final List<String> switchCooldowns;
    private final ArcheCore plugin;
    private final HelpDesk helpdesk;
    private final ArchePersonaHandler handler;
    private final int switchCooldownMinutes;
    
    public BeaconMenuListener(final ArcheCore plugin, final int switchCooldownMins) {
        super();
        this.switchCooldowns = Lists.newArrayList();
        this.plugin = plugin;
        this.helpdesk = HelpDesk.getInstance();
        this.handler = plugin.getPersonaHandler();
        this.switchCooldownMinutes = switchCooldownMins;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onClick(final InventoryClickEvent e) {
        final Inventory inv = e.getInventory();
        if (inv.getTitle() == ArcheBeacon.BEACON_HEADER) {
            e.setCancelled(true);
            final Player p = (Player)e.getWhoClicked();
            final int s = e.getRawSlot();
            switch (s) {
                case 1: {
                    new BukkitRunnable() {
                        public void run() {
                            BeaconMenuListener.this.helpdesk.openHelpMenu(p);
                        }
                    }.runTask((Plugin)this.plugin);
                    break;
                }
                case 2: {
                    new BukkitRunnable() {
                        public void run() {
                            p.closeInventory();
                            if (p.hasPermission("archecore.enderchest")) {
                                p.openInventory(p.getEnderChest());
                            }
                            else {
                                p.sendMessage(ChatColor.RED + "You do not have access to your Ender Chest.");
                            }
                        }
                    }.runTask((Plugin)this.plugin);
                    break;
                }
                case 3: {
                    SkillTome.consumeTomes(p);
                    new BukkitRunnable() {
                        public void run() {
                            p.closeInventory();
                        }
                    }.runTask((Plugin)this.plugin);
                    break;
                }
                case 4: {
                    this.helpdesk.outputHelp("persona", p);
                    new BukkitRunnable() {
                        public void run() {
                            p.closeInventory();
                        }
                    }.runTask((Plugin)this.plugin);
                    break;
                }
                case 0:
                case 5:
                case 6:
                case 7:
                case 8: {
                    final ArchePersona[] prs = this.handler.getAllPersonas((OfflinePlayer)p);
                    if (prs == null) {
                        this.plugin.getLogger().severe(" [Event] Player walking around without registered Personas File!");
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
                    if (s == 0 && current >= 0) {
                        final ArchePersona a = prs[current];
                        a.setXPGain(!a.getXPGain());
                        p.sendMessage(ChatColor.GRAY + "Toggled XP Gain for: " + a.getName());
                        new BukkitRunnable() {
                            public void run() {
                                p.closeInventory();
                            }
                        }.runTask((Plugin)this.plugin);
                        break;
                    }
                    if (s <= 4) {
                        break;
                    }
                    final int t = s - 5;
                    final CreationDialog dialog = new CreationDialog();
                    if (prs[t] == null) {
                        if (count < this.handler.getAllowedPersonas(p)) {
                            dialog.addPersona(p, t);
                            new BukkitRunnable() {
                                public void run() {
                                    p.closeInventory();
                                }
                            }.runTask((Plugin)this.plugin);
                            break;
                        }
                        break;
                    }
                    else if (e.isShiftClick()) {
                        if (e.isLeftClick()) {
                            dialog.addPersona(p, t);
                            new BukkitRunnable() {
                                public void run() {
                                    p.closeInventory();
                                }
                            }.runTask((Plugin)this.plugin);
                            break;
                        }
                        if (!e.isRightClick()) {
                            break;
                        }
                        new BukkitRunnable() {
                            public void run() {
                                p.closeInventory();
                            }
                        }.runTask((Plugin)this.plugin);
                        if (count > 1 || p.hasPermission("archecore.exempt")) {
                            dialog.removePersona(prs[t]);
                            break;
                        }
                        p.sendMessage(ChatColor.RED + "You may not remove your last Persona!");
                        break;
                    }
                    else {
                        if (current != t) {
                            final String pname = p.getName();
                            if (this.switchCooldowns.contains(pname)) {
                                p.sendMessage(ChatColor.RED + "You have a " + this.switchCooldownMinutes + " minute delay between switching Personas.");
                            }
                            else {
                                if (!p.hasPermission("archecore.persona.quickswitch")) {
                                    this.switchCooldowns.add(pname);
                                    new BukkitRunnable() {
                                        public void run() {
                                            BeaconMenuListener.this.switchCooldowns.remove(pname);
                                        }
                                    }.runTaskLater((Plugin)this.plugin, (long)(this.switchCooldownMinutes * 60 * 20));
                                }
                                this.handler.switchPersona(p, t);
                                p.sendMessage(ChatColor.AQUA + "You are now Roleplaying as: " + ChatColor.YELLOW + "" + ChatColor.ITALIC + prs[t].getName());
                            }
                            new BukkitRunnable() {
                                public void run() {
                                    p.closeInventory();
                                }
                            }.runTask((Plugin)this.plugin);
                            break;
                        }
                        break;
                    }
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true)
    public void onDrag(final InventoryDragEvent e) {
        final Inventory inv = e.getInventory();
        if (inv.getTitle() == ArcheBeacon.BEACON_HEADER) {
            e.setCancelled(true);
        }
    }
}
