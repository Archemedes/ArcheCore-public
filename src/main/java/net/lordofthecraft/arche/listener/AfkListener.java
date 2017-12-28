package net.lordofthecraft.arche.listener;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.Maps;

import net.lordofthecraft.arche.ArcheCore;

public class AfkListener implements Listener {
	public static final long AFK_THRESHOLD_MINUTES = 5;
	public Map<UUID, Long> lastAction = Maps.newHashMap();
	
	public boolean isAfk(Player p) {
		long lastSeen = lastAction.get(p.getUniqueId());
		long minsPassed = ( System.currentTimeMillis() - lastSeen) / (DateUtils.MILLIS_PER_MINUTE);
		return minsPassed >= AFK_THRESHOLD_MINUTES;
	}
	
	@EventHandler
	public void j(PlayerJoinEvent e) {
		go(e);
	}
	
	@EventHandler
	public void q(PlayerQuitEvent e) {
		lastAction.remove(e.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void i(PlayerInteractEvent e) {
		go(e);
	}
	
	@EventHandler
	public void c(PlayerCommandPreprocessEvent e) {
		go(e);
	}
	
	@EventHandler
	public void c(AsyncPlayerChatEvent e) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), ()->go(e));
	}
	
	private void go(PlayerEvent e) {
		lastAction.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
	}
	
}
