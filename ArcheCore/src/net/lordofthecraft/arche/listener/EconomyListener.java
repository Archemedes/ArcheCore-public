package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Economy;
import net.lordofthecraft.arche.interfaces.Persona;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

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

}
