package net.lordofthecraft.arche.persona;

import org.bukkit.Location;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;

public class PlaySession {
	private final Persona persona;
	private final long startTime;
	private final Location startLoc;
	private final int startPlaytime;
	
	PlaySession(Persona persona){
		this.persona = persona;
		startTime = System.currentTimeMillis();
		startLoc = persona.getPlayer().getLocation();
		startPlaytime = persona.getTimePlayed();
	}
	
	void endSession() {
		ArcheCore.getConsumerControls().insert("persona_playsessions")
			.set("login", startTime)
			.set("logout", System.currentTimeMillis())
			.set("locin", startLoc)
			.set("locout", persona.getPlayer().getLocation())
			.set("time_played", persona.getTimePlayed() - startPlaytime)
			.queue();
	}
	
	
}
