package net.lordofthecraft.arche.listener;

import org.bukkit.event.player.*;
import net.lordofthecraft.arche.persona.*;
import org.bukkit.event.*;

public class PlayerChatListener implements Listener
{
    private final ArchePersonaHandler handler;
    
    public PlayerChatListener() {
        super();
        this.handler = ArchePersonaHandler.getInstance();
    }
    
    @EventHandler
    public void onChat(final AsyncPlayerChatEvent e) {
        final ArchePersona pers = this.handler.getPersona(e.getPlayer());
        if (pers != null) {
            pers.addCharactersSpoken(e.getMessage().length());
        }
    }
}
