package net.lordofthecraft.arche.listener;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.entity.Player;

import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.CreationDialog;

public class PersonaCreationAbandonedListener implements ConversationAbandonedListener {
	
	@Override
	public void conversationAbandoned(ConversationAbandonedEvent e) {
		Player p = (Player) e.getContext().getForWhom();		
		p.resetTitle();
		
		if(ArchePersonaHandler.getInstance().countPersonas(p) == 0 ){
			if(p.hasPermission("archecore.exempt")){
				p.sendRawMessage(ChatColor.LIGHT_PURPLE + "You have permission to roam without a Persona");
				p.sendRawMessage(ChatColor.LIGHT_PURPLE + "But we recommend you use /bme to make one as soon as possible");
			} else {
				new CreationDialog().makeFirstPersona(p);
			}
		}
	
	}	
}
