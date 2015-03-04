package net.lordofthecraft.arche.listener;

import java.util.*;
import net.lordofthecraft.arche.*;
import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.entity.*;
import net.lordofthecraft.arche.persona.*;
import com.google.common.collect.*;

public class NewbieProtectListener implements Listener
{
    public static final Set<UUID> bonusProtects;
    private final ArchePersonaHandler handler;
    private final int protectDuration;
    
    public NewbieProtectListener(final ArchePersonaHandler handler, final int duration) {
        super();
        this.handler = handler;
        this.protectDuration = duration;
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDamage(final EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player target = (Player)e.getEntity();
            if (NewbieProtectListener.bonusProtects.contains(target.getUniqueId())) {
                target.sendMessage(ChatColor.GOLD + "The monks have deemed fit to guide you to safer places.");
                final World w = ArcheCore.getControls().getNewPersonaWorld();
                final Location to = (w == null) ? target.getWorld().getSpawnLocation() : w.getSpawnLocation();
                target.teleport(to);
            }
            if (e.getCause() == EntityDamageEvent.DamageCause.MAGIC && this.isNewbie(target)) {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onDamage(final EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player target = (Player)e.getEntity();
            final Entity d = e.getDamager();
            final boolean newbieTarget = this.isNewbie(target);
            if (d instanceof Player) {
                final Player p = (Player)d;
                if (this.isNewbie(p)) {
                    p.sendMessage(ChatColor.LIGHT_PURPLE + "You must further attune to your Persona before picking fights");
                    e.setCancelled(true);
                }
                else if (newbieTarget) {
                    p.sendMessage(ChatColor.LIGHT_PURPLE + "This Persona is shielded by a divine force... for now...");
                    e.setCancelled(true);
                }
            }
            else if (d instanceof Projectile && ((Projectile)d).getShooter() instanceof Player) {
                final Player p = (Player)((Projectile)d).getShooter();
                if (this.isNewbie(p)) {
                    p.sendMessage(ChatColor.LIGHT_PURPLE + "You must further attune to your Persona before picking fights");
                    e.setCancelled(true);
                }
                else if (newbieTarget) {
                    p.sendMessage(ChatColor.LIGHT_PURPLE + "This Persona is shielded by a divine force... for now...");
                    e.setCancelled(true);
                }
            }
        }
    }
    
    public boolean isNewbie(final Player p) {
        final ArchePersona pers = this.handler.getPersona(p);
        return !p.hasPermission("archecore.persona.nonewbie") && pers != null && pers.getTimePlayed() < this.protectDuration;
    }
    
    static {
        bonusProtects = Sets.newHashSet();
    }
}
