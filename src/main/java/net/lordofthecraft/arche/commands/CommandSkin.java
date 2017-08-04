package net.lordofthecraft.arche.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.lordofthecraft.arche.skin.MojangCommunicator.MinecraftAccount;
import net.lordofthecraft.arche.skin.SkinCache;

public class CommandSkin implements CommandExecutor {
	final private Plugin plugin;
	
	public CommandSkin(Plugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->
		{
			MinecraftAccount acc = new MinecraftAccount();
			
			
			
			try{ 
				//AuthenthicationData data = MojangCommunicator.authenthicate(acc);
				//MojangCommunicator.setSkin(data, "http://assets.mojang.com/SkinTemplates/alex.png");
				SkinCache.getSkinInfoFromPlayer((Player) sender); 
			}catch(Exception e) { throw new RuntimeException(e);}
			
		}
				
				);
		
		return true;
	}
}
