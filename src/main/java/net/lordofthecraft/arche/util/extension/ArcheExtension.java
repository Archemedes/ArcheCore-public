package net.lordofthecraft.arche.util.extension;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.util.InventoryUtil;
import net.lordofthecraft.arche.util.ItemUtil;
import net.lordofthecraft.arche.util.WeakBlock;

/**
 * For use with MethodExtension (e.g. Lombok).
 * Bunch of gross hacks to extend Bukkit API.
 */
public class ArcheExtension {
	private ArcheExtension() {}
	
	public static Optional<Persona> persona(Player p){
		if(p == null) return Optional.empty();
		else return Optional.ofNullable(ArcheCore.getPersona(p));
	}
	
	public static Optional<Persona> persona(Entity e){
		if(e instanceof Player) return persona((Player) e);
		else return Optional.empty();
	}
	
	public static Optional<Persona> persona(String playerName){
		return persona(Bukkit.getPlayer(playerName));
	}
	
	public static Optional<Persona> persona(CommandSender sender){
		if(sender instanceof Player) return persona((Player) sender);
		else return Optional.empty();
	}
	
	public static Optional<Persona> persona(UUID uuid){
		return persona(Bukkit.getPlayer(uuid));
	}
	
	public static Optional<Persona> getPersona(PlayerEvent event){
		return persona(event.getPlayer());
	}
	
	public static boolean exists(ItemStack is) {
		return ItemUtil.exists(is);
	}
	
	public static void addItemObeyStackSize(Inventory inv, ItemStack... items) {
		InventoryUtil.addItem(inv, items);
	}
	
	public static void run(JavaPlugin plugin, Runnable task) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task);
	}
	
	public static void run(JavaPlugin plugin, Runnable task, long delayed) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, task, delayed);
	}
	
	public static void run(JavaPlugin plugin, Runnable task, long delay, long period) {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, delay, period);
	}
	
	public static WeakBlock getWeakBlock(Location l) {
		return new WeakBlock(l);
	}
	
}
