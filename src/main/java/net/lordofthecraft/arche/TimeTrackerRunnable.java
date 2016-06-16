package net.lordofthecraft.arche;

import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TimeTrackerRunnable extends BukkitRunnable{
	private final ArchePersonaHandler psh;
	
	/*Runs roughly once every minute
	Adds a minute tick to each of the Online Players' current Personas
	Rough activity of the Persona thus measured*/
	
	TimeTrackerRunnable(ArchePersonaHandler psh){
		this.psh = psh;
	}
	
	@Override
	public void run() {
		for(Player p : Bukkit.getServer().getOnlinePlayers()){
			ArchePersona pers = psh.getPersona(p);
			if(pers != null){
				pers.addTimePlayed(1);
			}
		}
	}

}
