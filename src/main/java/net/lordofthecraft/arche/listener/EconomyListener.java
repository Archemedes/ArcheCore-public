package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SimpleTransaction;
import net.lordofthecraft.arche.interfaces.Economy;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Transaction.TransactionType;
import net.lordofthecraft.arche.util.MessageUtil;

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
			economy.withdrawPersona(p, penalty, new SimpleTransaction("Death penalty for " + MessageUtil.identifyPersona(p), TransactionType.WITHDRAW));
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent e){
		if (e.getRightClicked() instanceof Villager){
			e.setCancelled(true);
			e.getRightClicked().remove();
		}
	}
}
