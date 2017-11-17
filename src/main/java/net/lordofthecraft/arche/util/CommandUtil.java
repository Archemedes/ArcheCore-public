package net.lordofthecraft.arche.util;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.persona.ArcheOfflinePersona;
import net.lordofthecraft.arche.persona.ArchePersona;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.stream.Collectors;

public class CommandUtil {	
	public static Player getPlayerOrMessage(CommandSender sender) {
		return getPlayerOrMessage(sender, ChatColor.DARK_RED + "ERROR: " + ChatColor.RED + " This command must be run by a player");
	}
	
	public static Player getPlayerOrMessage(CommandSender sender, String message) {
		if(sender instanceof Player) {
			return (Player) sender;
		} else {
			sender.sendMessage(message);
			return null;
		}
	}

	public static Persona personaFromArg(String a){
        OfflinePersona persona = offlinePersonaFromArg(a);
        if (persona != null && persona instanceof ArchePersona) {
            return (Persona) persona;
        } else {
            return null;
        }
    }

    public static OfflinePersona offlinePersonaFromArg(String a) {
        int c = StringUtils.countMatches(a, "@");
        String player;
        int id = -1;

        if (c == 1) {
            String[] s = a.split("@");
            player = s[0];
            try {
                id = Integer.parseInt(s[1]);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (c == 0) {
            player = a;
        } else return null;

        PersonaHandler hand = ArcheCore.getControls().getPersonaHandler();

        @SuppressWarnings("deprecation")
        OfflinePlayer p = Bukkit.getOfflinePlayer(player);
        
        if (p == null) {
            return null;
        }
        return id < 0 || id > ArcheCore.getControls().personaSlots() ?
                hand.getPersona(p) : hand.getAllPersonas(p)[id];
        
    }
	
	public static Persona currentPersonaFromArg(String a){
		Player p = Bukkit.getPlayer(a);
		if(p == null) return getPreloadedPersonaFromName(a);
		else return ArcheCore.getControls().getPersonaHandler().getPersona(p);
	}

	private static Persona getPreloadedPersonaFromName(String name) {
		PersonaHandler hand = ArcheCore.getControls().getPersonaHandler();
        for (Persona persona : hand.getPersonas().parallelStream().filter(ArcheOfflinePersona::isLoaded).map(ArchePersona.class::cast).collect(Collectors.toList())) {
            if (persona == null) continue;
            else if (persona.getPlayerName().equals(name)) return persona;
            else break;
        }
		return null;
	}

    private static OfflinePersona getPersonaFromName(UUID player) {
        PersonaHandler hand = ArcheCore.getControls().getPersonaHandler();
        /*for(Persona persona : hand.getPersonas().parallelStream().filter(p -> p instanceof ArchePersona).map(ArchePersona.class::cast).collect(Collectors.toList())) {
            if(persona.getPlayerName().equals(name)) return persona;
		}*/
        return hand.getPersonas()
                .parallelStream()
                .filter(p -> p.getPlayerUUID().equals(player) && p.isCurrent())
                .findFirst()
                .orElse(null);
    }
}
