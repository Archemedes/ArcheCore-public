package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheFatigueHandler;
import net.lordofthecraft.arche.interfaces.FatigueHandler;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.save.rows.persona.FatigueReduceRow;

import org.apache.commons.lang.Validate;
import org.bukkit.scheduler.BukkitRunnable;

public class FatigueDecreaser extends BukkitRunnable {
	private final PersonaHandler handler;
	private final FatigueHandler fatigue;
	private double fatigueRestoreHours;

	public FatigueDecreaser(int decrease) {
		Validate.isTrue(decrease > 0);
		this.handler = ArchePersonaHandler.getInstance();
		this.fatigue = ArcheFatigueHandler.getInstance();
		this.fatigueRestoreHours = (double) decrease;
	}

	@Override
	public void run() {
		double toDecrease = 100 / (fatigueRestoreHours * 3);

		ArcheCore.getConsumerControls().queueRow(new FatigueReduceRow());
		
		handler.getPersonas().stream()
			.filter(prs -> prs instanceof Persona)
			.map(ArchePersona.class::cast)
			.forEach(pr->{
				double fat = pr.getFatigue();
				fat = Math.max(0.0, fat - toDecrease);
				pr.setFatigueRaw(fat);
				if(pr.isCurrent()) fatigue.showFatigueBar(pr);
			});
	}
}
