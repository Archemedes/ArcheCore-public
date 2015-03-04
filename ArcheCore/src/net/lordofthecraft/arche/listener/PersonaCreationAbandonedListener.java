package net.lordofthecraft.arche.listener;

import org.bukkit.conversations.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import net.lordofthecraft.arche.persona.*;

public class PersonaCreationAbandonedListener implements ConversationAbandonedListener
{
    public void conversationAbandoned(final ConversationAbandonedEvent e) {
        final Player p = (Player)e.getContext().getForWhom();
        System.out.println("[ArcheCore] Reached Abandoned listener");
        if (ArchePersonaHandler.getInstance().countPersonas(p) == 0) {
            if (p.hasPermission("archecore.exempt")) {
                p.sendRawMessage(ChatColor.LIGHT_PURPLE + "You have permission to roam without a Persona");
                p.sendRawMessage(ChatColor.LIGHT_PURPLE + "We recommend you visit a beacon, however (or /beaconme)");
            }
            else {
                new CreationDialog().makeFirstPersona(p);
            }
        }
    }
}
