package net.lordofthecraft.arche;

import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.AllArgsConstructor;
import lombok.var;
import net.lordofthecraft.arche.account.ArcheAccountHandler;
import net.lordofthecraft.arche.interfaces.IArcheCore;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;

@AllArgsConstructor
public class TimeTrackerRunnable extends BukkitRunnable{
	private final IArcheCore iac;
	private final ArcheAccountHandler aah;
	private final ArchePersonaHandler psh;
	
	/*Runs roughly once every minute
	Adds a minute tick to each of the Online Players' current Personas
	Rough activity of the Persona thus measured*/
	
	@Override
	public void run() {
		var ps = Bukkit.getOnlinePlayers().stream()
			.filter(p -> !iac.isAfk(p))
			.collect(Collectors.toList());
		
			ps.stream()
			.map(psh::getPersona)
			.filter(Objects::nonNull)
			.forEach(pers -> pers.addTimePlayed(1));
			
			ps.stream()
			.map(aah::getAccount)
			.forEach(a -> a.addTimePlayed(1));
			
	}
}
