package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Economy;
import net.lordofthecraft.arche.interfaces.Persona;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class EconomyListener implements Listener {
	private final Economy economy;
	
	public EconomyListener(Economy economy) {
		this.economy = economy;
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e){
		Persona p = ArcheCore.getControls().getPersonaHandler().getPersona(e.getEntity());
		if(p != null){
			double penalty = economy.getBalance(p) * economy.getFractionLostOnDeath();
			economy.withdrawPersona(p, penalty);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent e){
		if (e.getRightClicked() instanceof Villager){
			e.setCancelled(true);
			Player pl = e.getPlayer();
			Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission("archecore.mod") || p.hasPermission("archecore.admin")
					&& !pl.hasPermission("archecore.mod") && !p.hasPermission("archecore.admin")).forEach(p -> {
				p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[WARNING] " + ChatColor.RESET + "" + ChatColor.DARK_RED + pl.getName() + " attempted to trade with a villager!");
			});
		}
	}
}
