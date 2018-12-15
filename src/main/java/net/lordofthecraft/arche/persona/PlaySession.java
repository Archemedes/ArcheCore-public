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
		WeakBlock wb = new WeakBlock(persona.getPlayer().getLocation());
		long elapsed = persona.getTimePlayed() - startPlaytime;
		if(elapsed < 2) return;
		
		ArcheCore.getConsumerControls().insert("persona_sessions")
			.set("persona_id_fk", persona.getPersonaId())
			.set("login", startTime)
			.set("logout", System.currentTimeMillis())
			.set("world", wb.getWorld())
			.set("x", wb.getX())
			.set("y", wb.getY())
			.set("z", wb.getZ())
			.set("time_played", elapsed)
			.queue();
	}
	
	
}
