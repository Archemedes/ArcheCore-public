package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheFatigueHandler;
import net.lordofthecraft.arche.interfaces.FatigueHandler;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.save.rows.persona.FatigueReduceRow;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.stream.Collectors;

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
        //TODO fix
        double toDecrease = 100 / (fatigueRestoreHours * 3);

        //ArcheExecutor.getInstance().put(new FatigueReduceTask());
        ArcheCore.getConsumerControls().queueRow(new FatigueReduceRow());

        for (Persona pr : handler.getPersonas().stream().filter(prs -> prs instanceof Persona).map(ArchePersona.class::cast).collect(Collectors.toList())) {
            if (pr == null) continue;
            fatigue.reduceFatigue(pr, toDecrease);
            if (pr.isCurrent()) fatigue.showFatigueBar(pr);
        }
	}

}
