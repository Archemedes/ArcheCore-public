package net.lordofthecraft.arche.listener;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import co.lotc.core.bukkit.util.WeakBlock;
import lombok.Value;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.event.PlayerAfkEvent;

public class AfkListener implements Listener {
	public static final long AFK_THRESHOLD_MINUTES = 5;
	
	private final ArcheCore plugin;
	
	public Map<UUID, Seen> lastAction = Maps.newHashMap();
	public Set<UUID> theAfks = Sets.newHashSet();
	
	public AfkListener(ArcheCore plugin) {
		this.plugin = plugin;
		new BukkitRunnable() {
			@Override
			public void run() {
					for(Player p : Bukkit.getOnlinePlayers()) {
						if(theAfks.contains(p.getUniqueId())) continue;
						
						long lastSeen = lastAction.get(p.getUniqueId()).getWhen();
						long minsPassed = ( System.currentTimeMillis() - lastSeen) / (DateUtils.MILLIS_PER_MINUTE);
						if(minsPassed >= AFK_THRESHOLD_MINUTES) {
							CoreLog.debug("Player has gone afk: " + p.getName());
							theAfks.add(p.getUniqueId());
							Bukkit.getPluginManager().callEvent(new PlayerAfkEvent(p, true));
						}
				}
			}
		}.runTaskTimer(plugin, 11273L, 53L);
	}
	
	public boolean isAfk(Player p) {
		return theAfks.contains(p.getUniqueId());
	}
		
	@EventHandler
	public void j(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		lastAction.put(p.getUniqueId(), new Seen(System.currentTimeMillis(), new WeakBlock(p.getLocation())));
	}
	
	@EventHandler
	public void q(PlayerQuitEvent e) {
		lastAction.remove(e.getPlayer().getUniqueId());
		theAfks.remove(e.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void i(PlayerInteractEvent e) {
		go(e);
	}

	@EventHandler
	public void i(PlayerInteractEntityEvent e) {
		go(e);
	}
	
	@EventHandler
	public void c(PlayerCommandPreprocessEvent e) {
		go(e);
	}
	
	@EventHandler
	public void c(PlayerDropItemEvent e) {
		go(e);
	}
	
	@EventHandler
	public void c(InventoryInteractEvent e) {
		go((Player) e.getWhoClicked());
	}
	
	@EventHandler
	public void c(InventoryOpenEvent e) {
		go((Player) e.getPlayer());
	}
	
	@EventHandler
	public void c(InventoryCloseEvent e) {
		go((Player) e.getPlayer());
	}
	
	@EventHandler
	public void c(AsyncPlayerChatEvent e) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->go(e, false));
	}
	
	
	private void go(PlayerEvent e) {
		go(e,true);
	}
	
	private void go(PlayerEvent e, boolean strict) {
		go(e.getPlayer(), strict);
	}
	
	private void go(Player p) {
		go(p, true);
	}
	
	private void go(Player p, boolean strict) {
		UUID u = p.getUniqueId();
		WeakBlock wb = new WeakBlock(p.getLocation());
		
		if(strict) {
			Seen seen = lastAction.get(u);
			
			//If player didnt move at all it doesnt count as active
			if(wb.equals(seen.getWhere())) return;
		}
		
		lastAction.put(u, new Seen(System.currentTimeMillis(), wb));
		
		if(theAfks.contains(u)) {
			CoreLog.debug("Player is no longer afk: " + p.getName());
			theAfks.remove(u);
			Bukkit.getPluginManager().callEvent(new PlayerAfkEvent(p, false));
		}
	}
	
	@Value
	private static class Seen{
		long when; WeakBlock where;
	}
	
}
