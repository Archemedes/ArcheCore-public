package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.util.WeakBlock;

public class PlaySession {
	private final Persona persona;
	private final long startTime;
	private final int startPlaytime;
	
	PlaySession(Persona persona){
		this.persona = persona;
		startTime = System.currentTimeMillis();
		startPlaytime = persona.getTimePlayed();
	}
	
	void endSession() {
		ArcheCore.getConsumerControls().insert("persona_playsessions")
			.set("persona_id_fk", persona.getPersonaId())
			.set("login", startTime)
			.set("logout", System.currentTimeMillis())
			.set("loc", new WeakBlock(persona.getPlayer().getLocation()))
			.set("time_played", persona.getTimePlayed() - startPlaytime)
			.queue();
	}
	
	
}
