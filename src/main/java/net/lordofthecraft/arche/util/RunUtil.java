package net.lordofthecraft.arche.util;

import java.util.concurrent.Executor;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class RunUtil {

	public static Executor syncExecutor(Plugin plugin) {
		return (r->Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, r));
	}
	
	public static Executor asyncExecutor(Plugin plugin) {
		return (r->Bukkit.getScheduler().runTaskAsynchronously(plugin,r));
	}
	
}
