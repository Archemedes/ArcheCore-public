package net.lordofthecraft.arche.save.tasks;

import java.util.UUID;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.event.AsyncPlayerUnloadEvent;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class UnloadTask extends ArcheTask {
	private final String name;
	private final UUID uuid;
	
	public UnloadTask(Player who) {
		super();
		this.name = who.getName();
		this.uuid = who.getUniqueId();
	}
	
	
	public void run(){
		Plugin plugin = (Plugin) ArcheCore.getControls();
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new UnloadRunnable());
		Bukkit.getPluginManager().callEvent(new AsyncPlayerUnloadEvent(name, uuid));
	}
	
	private class UnloadRunnable implements Runnable{
		
		@Override
		public void run(){
			ArchePersonaHandler h = (ArchePersonaHandler) ArcheCore.getControls().getPersonaHandler();
			h.unload(uuid);
		}
	}
}
