package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.CreationDialog;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.entity.Player;

public class PersonaCreationAbandonedListener implements ConversationAbandonedListener {
	
	@Override
	public void conversationAbandoned(ConversationAbandonedEvent e) {
		Player p = (Player) e.getContext().getForWhom();
		System.out.println("[ArcheCore] Reached Abandoned listener");
		
		if(ArchePersonaHandler.getInstance().countPersonas(p) == 0 ){
			if(p.hasPermission("archecore.exempt")){
				p.sendRawMessage(ChatColor.LIGHT_PURPLE + "You have permission to roam without a Persona");
				p.sendRawMessage(ChatColor.LIGHT_PURPLE + "We recommend you visit a beacon, however (or /beaconme)");
			} else {
				new CreationDialog().makeFirstPersona(p);
			}
		}
	
	}	
}
