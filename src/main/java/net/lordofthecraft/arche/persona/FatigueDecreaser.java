package net.lordofthecraft.arche.persona;

import org.bukkit.scheduler.BukkitRunnable;

import net.lordofthecraft.arche.ArcheFatigueHandler;
import net.lordofthecraft.arche.interfaces.FatigueHandler;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;

public class FatigueDecreaser extends BukkitRunnable {
	private final PersonaHandler handler;
	private final FatigueHandler fatigue;
	private double fatigueRestoreHours;
	
	public FatigueDecreaser(int decrease) {
		this.handler = ArchePersonaHandler.getInstance();
		this.fatigue = ArcheFatigueHandler.getInstance();
		this.fatigueRestoreHours = (double) decrease;
	}

	@Override
	public void run() {
		//Assumes it runs every 20 minutes
		double toDecrease = FatigueHandler.MAXIMUM_FATIGUE / (fatigueRestoreHours * 3);
		
		// TODO SQL Task call here
		
		for(Persona[] prs: handler.getPersonas()) {
			for(Persona pr : prs) {
				fatigue.reduceFatigue(pr, toDecrease);
				if(pr.isCurrent()) fatigue.showFatigueBar(pr);
			}
		}
	}

}
