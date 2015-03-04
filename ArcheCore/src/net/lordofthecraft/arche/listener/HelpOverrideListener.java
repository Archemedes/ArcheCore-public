package net.lordofthecraft.arche.listener;

import org.bukkit.event.player.*;
import org.bukkit.event.*;

public class HelpOverrideListener implements Listener
{
    @EventHandler
    public void onCommand(final PlayerCommandPreprocessEvent e) {
        final String message = e.getMessage();
        if (message.startsWith("/help")) {
            if (message.length() == 5 || message.charAt(5) == ' ') {
                e.setMessage("/archehelp" + message.substring(5));
            }
        }
        else if (message.startsWith("/oldhelp") && (message.length() == 8 || message.charAt(8) == ' ')) {
            e.setMessage("/help" + message.substring(8));
        }
    }
}
