package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.persona.*;
import org.bukkit.entity.*;
import java.util.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.event.block.*;
import net.lordofthecraft.arche.*;
import org.bukkit.*;

public class PlayerInteractListener implements Listener
{
    private final ArchePersonaHandler handler;
    
    public PlayerInteractListener(final ArcheCore plugin) {
        super();
        this.handler = plugin.getPersonaHandler();
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onClickPlayer(final PlayerInteractEntityEvent e) {
        final Player p = e.getPlayer();
        if (e.getRightClicked() instanceof Player && p.isSneaking()) {
            final Player target = (Player)e.getRightClicked();
            for (final String x : this.handler.whois(target)) {
                p.sendMessage(x);
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onClickBeacon(final PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            final Material m = e.getClickedBlock().getType();
            final Player p = e.getPlayer();
            if (m == Material.BEACON) {
                if (!p.isSneaking() || !p.hasPermission("archecore.vanillabeacon")) {
                    e.setCancelled(true);
                    if (p.hasPermission("archecore.mayuse")) {
                        ArcheBeacon.openBeacon(p);
                    }
                }
            }
            else if (m == Material.ENDER_CHEST && !p.hasPermission("archecore.enderchest")) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.RED + "You do not have access to your Ender Chest.");
            }
        }
    }
}
