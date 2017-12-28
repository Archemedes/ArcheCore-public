package net.lordofthecraft.arche;

import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import net.lordofthecraft.arche.interfaces.IArcheCore;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;

public class TimeTrackerRunnable extends BukkitRunnable{
	private final ArchePersonaHandler psh;
	private final IArcheCore iac;
	
	/*Runs roughly once every minute
	Adds a minute tick to each of the Online Players' current Personas
	Rough activity of the Persona thus measured*/
	
	TimeTrackerRunnable(ArchePersonaHandler psh){
		this.psh = psh;
		this.iac = ArcheCore.getControls();
	}
	
	@Override
	public void run() {
		Bukkit.getOnlinePlayers().stream()
			.filter(p -> !iac.isAfk(p))
			.map(psh::getPersona)
			.filter(Objects::nonNull)
			.forEach(pers -> pers.addTimePlayed(1));
	}
}
