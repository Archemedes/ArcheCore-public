package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.*;
import com.google.common.collect.*;
import net.lordofthecraft.arche.enums.*;
import net.lordofthecraft.arche.attributes.*;
import net.lordofthecraft.arche.interfaces.*;
import net.lordofthecraft.arche.persona.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.*;
import org.bukkit.plugin.*;
import org.bukkit.potion.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;
import org.bukkit.event.entity.*;

public class RacialBonusListener implements Listener
{
    private final Random rnd;
    private final ArchePersonaHandler handler;
    private final ArcheCore plugin;
    private final List<String> sneakAttempts;
    private final List<String> sneakers;
    
    public RacialBonusListener(final ArcheCore plugin, final ArchePersonaHandler handler) {
        super();
        this.rnd = new Random();
        this.sneakAttempts = Lists.newArrayList();
        this.sneakers = Lists.newArrayList();
        this.plugin = plugin;
        this.handler = handler;
    }
    
    private boolean hasTogglePower(final Race race) {
        switch (race) {
            case DARK_ELF:
            case KHARAJYR:
            case KHA_CHEETRAH:
            case KHA_PANTERA:
            case KHA_LEPARDA:
            case KHA_TIGRASI: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    @EventHandler
    public void onRespawn(final PlayerRespawnEvent e) {
        final Player p = e.getPlayer();
        AttributeBase.clearModifiers((LivingEntity)p, AttributeType.MOVEMENT_SPEED);
        AttributeBase.clearModifiers((LivingEntity)p, AttributeType.ATTACK_DAMAGE);
        final Persona ps = this.handler.getPersona(p);
        if (ps == null) {
            RaceBonusHandler.reset(p);
        }
        else {
            RaceBonusHandler.apply(p, ps.getRace());
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onHunger(final FoodLevelChangeEvent e) {
        final Player p = (Player)e.getEntity();
        if (p.getFoodLevel() - e.getFoodLevel() == 1) {
            final ArchePersona pers = this.handler.getPersona(p);
            if (pers != null) {
                final Race race = pers.getRace();
                if (race == Race.ORC || race == Race.OLOG) {
                    p.removePotionEffect(PotionEffectType.HUNGER);
                    if (race == Race.OLOG && p.isSprinting()) {
                        e.setFoodLevel(Math.max(0, e.getFoodLevel() - 3));
                    }
                }
                else if (this.rnd.nextBoolean()) {
                    e.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSneak(final PlayerToggleSneakEvent e) {
        final Player p = e.getPlayer();
        if (e.isSneaking()) {
            final ArchePersona pers = this.handler.getPersona(p);
            if (pers != null && this.hasTogglePower(pers.getRace()) && !this.sneakers.contains(p.getName())) {
                if (!this.sneakAttempts.contains(p.getName())) {
                    this.sneakAttempts.add(p.getName());
                    new BukkitRunnable() {
                        public void run() {
                            RacialBonusListener.this.sneakAttempts.remove(p.getName());
                        }
                    }.runTaskLater((Plugin)this.plugin, 12L);
                }
                else {
                    if (pers.getRace() == Race.DARK_ELF) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 120, 1, true), true);
                    }
                    else {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 450, 2, true), true);
                        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 120, 1, true), true);
                    }
                    p.playSound(p.getLocation(), Sound.AMBIENCE_CAVE, 0.8f, 2.0f);
                    this.sneakers.add(p.getName());
                    new BukkitRunnable() {
                        public void run() {
                            RacialBonusListener.this.sneakers.remove(p.getName());
                            p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1.0f, 1.0f);
                        }
                    }.runTaskLater((Plugin)this.plugin, 600L);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(final EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Arrow) {
            if (((Arrow)e.getDamager()).getShooter() instanceof Player) {
                final Player p = (Player)((Arrow)e.getDamager()).getShooter();
                final ArchePersona pers = this.handler.getPersona(p);
                if (pers != null && pers.getRace() == Race.WOOD_ELF) {
                    double dmg = e.getDamage();
                    dmg *= 1.2;
                    e.setDamage(dmg);
                }
            }
        }
        else if (e.getDamager() instanceof Player) {
            final Player p = (Player)e.getDamager();
            final ArchePersona pers = this.handler.getPersona(p);
            if (pers != null) {
                double dmg = e.getDamage();
                final Race r = pers.getRace();
                switch (r) {
                    case ORC: {
                        final double fract = p.getHealth() / p.getMaxHealth();
                        if (fract < 0.2) {
                            dmg *= 1.25;
                        }
                        else if (fract < 0.5) {
                            dmg *= 1.1;
                        }
                        e.setDamage(dmg);
                        break;
                    }
                    case HUMAN:
                    case SOUTHERON:
                    case NORTHENER: {
                        if (e.getEntity() instanceof Player) {
                            int count = 0;
                            for (final Entity ent : p.getNearbyEntities(6.0, 3.0, 6.0)) {
                                if (ent instanceof Player) {
                                    final ArchePersona x = this.handler.getPersona((Player)ent);
                                    if (x != null && (x.getRace() == Race.HUMAN || x.getRace() == Race.NORTHENER || x.getRace() == Race.SOUTHERON) && ++count >= 4) {
                                        dmg *= 1.15;
                                        e.setDamage(dmg);
                                        break;
                                    }
                                    continue;
                                }
                            }
                            break;
                        }
                        break;
                    }
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(final EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            final EntityDamageEvent.DamageCause c = e.getCause();
            if (c == EntityDamageEvent.DamageCause.MAGIC) {
                double dmg = e.getDamage();
                final ArchePersona pers = this.handler.getPersona((Player)e.getEntity());
                if (pers != null && pers.getRace() == Race.HIGH_ELF) {
                    dmg *= 0.75;
                    e.setDamage(dmg);
                }
            }
            else if (c == EntityDamageEvent.DamageCause.FALL) {
                double dmg = e.getDamage();
                final ArchePersona pers = this.handler.getPersona((Player)e.getEntity());
                if (pers != null) {
                    switch (pers.getRace()) {
                        case KHARAJYR:
                        case KHA_CHEETRAH:
                        case KHA_PANTERA:
                        case KHA_LEPARDA:
                        case KHA_TIGRASI: {
                            dmg -= 4.0;
                            if (dmg <= 0.0) {
                                e.setCancelled(true);
                                break;
                            }
                            e.setDamage(dmg);
                            break;
                        }
                    }
                }
            }
        }
    }
}
