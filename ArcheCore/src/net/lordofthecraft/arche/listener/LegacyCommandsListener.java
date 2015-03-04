package net.lordofthecraft.arche.listener;

import org.bukkit.event.player.*;
import org.bukkit.event.*;

public class LegacyCommandsListener implements Listener
{
    @EventHandler
    public void onCommand(final PlayerCommandPreprocessEvent e) {
        String cmd = e.getMessage();
        if (cmd.startsWith("/set")) {
            cmd = cmd.substring(4);
            if (cmd.startsWith("info")) {
                e.setMessage("/persona setbio" + cmd.substring(4));
            }
            else if (cmd.startsWith("name")) {
                e.setMessage("/persona name" + cmd.substring(4));
            }
            else if (cmd.startsWith("age")) {
                e.setMessage("/persona age" + cmd.substring(3));
            }
            else if (cmd.startsWith("autoage")) {
                e.setMessage("/persona autoage" + cmd.substring(7));
            }
        }
        else if (cmd.startsWith("/char") || cmd.startsWith("/card")) {
            if (cmd.length() == 5 || cmd.charAt(5) == ' ') {
                e.setMessage("/persona view" + cmd.substring(5));
            }
        }
        else if (cmd.startsWith("/addinfo")) {
            e.setMessage("/persona addbio" + cmd.substring(8));
        }
    }
}
