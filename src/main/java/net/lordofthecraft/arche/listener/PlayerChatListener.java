package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {
	private final ArchePersonaHandler handler;
	
	public PlayerChatListener(){
		handler = ArchePersonaHandler.getInstance();
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e){
		ArchePersona pers = handler.getPersona(e.getPlayer());
		if(pers != null)
			pers.addCharactersSpoken(e.getMessage().length());
	}
}
