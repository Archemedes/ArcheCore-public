package net.lordofthecraft.arche.executables;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class OpenEnderRunnable implements Runnable {

    private final Persona pers;
    private final Location start;

    private OpenEnderRunnable(Persona p, Location start) {
        this.pers = p;
        this.start = start;
    }

    public static void begin(Persona pers) {
        //TODO cooldown
        Player p = pers.getPlayer();
        if (p != null) {
            if (!p.hasPermission("archecore.mod")) {
                if(p.getWorld().getName().equals("war")){
                    p.sendMessage(ChatColor.RED + "Cannot open your enderchest here!");
                    return;
                }
                OpenEnderRunnable r = new OpenEnderRunnable(pers, pers.getPlayer().getLocation());
                p.getWorld().playSound(pers.getPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
                p.sendMessage(ChatColor.AQUA + "Opening your enderchest. Don't move or you will interrupt the process.");
                Bukkit.getScheduler().runTaskLater(ArcheCore.getPlugin(), r, 5 * 20);
            } else {
                p.openInventory(p.getEnderChest());
            }
        }
    }

    @Override
    public void run() {
        Player p = pers.getPlayer();
        if (p != null) {
            if (p.getWorld() == start.getWorld() && p.getLocation().distanceSquared(start) < 1) {
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_CHEST_OPEN, 1, 1);
                p.closeInventory();
                p.openInventory(p.getEnderChest());
            } else {
                p.sendMessage(ChatColor.RED + "You were interrupted in opening your enderchest.");
            }
        }
    }
}
