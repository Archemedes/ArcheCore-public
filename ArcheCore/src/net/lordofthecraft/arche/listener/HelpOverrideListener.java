package net.lordofthecraft.arche.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class HelpOverrideListener implements Listener{

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent e){
		String message = e.getMessage();
		
		if(message.startsWith("/help")){
			if(message.length() == 5 || message.charAt(5) == ' ')
				e.setMessage("/archehelp" + message.substring(5));
		}else if(message.startsWith("/oldhelp")){
			if(message.length() == 8 || message.charAt(8) == ' ')
				e.setMessage("/help" + message.substring(8));
		}		
	}
}
