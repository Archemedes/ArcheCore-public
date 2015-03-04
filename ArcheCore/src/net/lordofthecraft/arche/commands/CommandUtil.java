package net.lordofthecraft.arche.commands;

import org.apache.commons.lang.*;
import net.lordofthecraft.arche.*;
import org.bukkit.*;
import net.lordofthecraft.arche.interfaces.*;

public class CommandUtil
{
    public static Persona personaFromArg(final String a) {
        final int c = StringUtils.countMatches(a, "@");
        int id = -1;
        String player;
        if (c == 1) {
            final String[] s = a.split("@");
            player = s[0];
            try {
                id = Integer.parseInt(s[1]);
            }
            catch (NumberFormatException e) {
                return null;
            }
        }
        else {
            if (c != 0) {
                return null;
            }
            player = a;
        }
        OfflinePlayer p = (OfflinePlayer)Bukkit.getPlayer(player);
        if (p == null) {
            p = Bukkit.getOfflinePlayer(player);
        }
        if (p == null) {
            return null;
        }
        final PersonaHandler hand = ArcheCore.getControls().getPersonaHandler();
        return (id < 0 || id > 3) ? hand.getPersona(p) : hand.getAllPersonas(p)[id];
    }
    
    public static Persona currentPersonaFromArg(final String a) {
        OfflinePlayer p = (OfflinePlayer)Bukkit.getPlayer(a);
        if (p == null) {
            p = Bukkit.getOfflinePlayer(a);
        }
        if (p == null) {
            return null;
        }
        return ArcheCore.getControls().getPersonaHandler().getPersona(p);
    }
}
