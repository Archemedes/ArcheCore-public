package net.lordofthecraft.arche.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.util.ItemUtil;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheCoreTransaction;
import net.lordofthecraft.arche.interfaces.Economy;
import net.lordofthecraft.arche.interfaces.Persona;

public class EconomyListener implements Listener {
	private final Economy economy;

	public EconomyListener(Economy economy) {
		this.economy = economy;
	}

	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDeath(PlayerDeathEvent e) {
		Persona p = ArcheCore.getControls().getPersonaHandler().getPersona(e.getEntity());
		if (p != null) {
			double penalty = economy.getBalance(p) * economy.getFractionLostOnDeath();
			if (penalty <= 0) return;
			economy.withdrawPersona(p, penalty, new ArcheCoreTransaction(p.identify() + " received a death penalty"));
			ItemStack i = economy.getPhysical(penalty);
			p.getPlayer().getWorld().dropItemNaturally(p.getPlayer().getLocation(), i);
		}
	}
	
	@EventHandler
	public void onMinaHopper(InventoryPickupItemEvent e) {
		if (e.getItem().getItemStack().getType() == Material.GOLD_NUGGET) {
			ItemStack is = e.getItem().getItemStack();
			if(ItemUtil.hasCustomTag(is, "mina"))
				e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent e) {
		if (e.getRightClicked() instanceof Villager) {
			e.setCancelled(true);
			e.getRightClicked().remove();
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerPickup(EntityPickupItemEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();
		if (e.getItem().getItemStack().getType() == Material.GOLD_NUGGET) {
			ItemStack is = e.getItem().getItemStack();
			String s = ItemUtil.getCustomTag(is, "mina");
			if (s != null) {
				double amt = Double.valueOf(s)*is.getAmount();
				Persona pers = ArcheCore.getControls().getPersonaHandler().getPersona(p);
				ArcheCore.getControls().getEconomy().depositPersona(pers, amt, new ArcheCoreTransaction(pers.identify() + " picked up off the ground"));
				p.getWorld().playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
				p.sendMessage(ChatColor.AQUA + "You pick up " + ChatColor.GOLD + amt + ChatColor.AQUA + " " + (amt == 1 ? economy.currencyNameSingular() : economy.currencyNamePlural()));
				e.setCancelled(true);
				e.getItem().remove();
			}
		}
	}
}
