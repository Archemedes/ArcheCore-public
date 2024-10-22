package net.lordofthecraft.arche.util;

import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.lotc.core.agnostic.Sender;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.persona.ArcheOfflinePersona;
import net.lordofthecraft.arche.persona.ArchePersona;

public class CommandUtil {
	private CommandUtil() {}
	
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

	public static Persona senderOrPersonaFromArg(Sender s, String a) {
		if("@s".equals(a)) a = s.getName();
		return personaFromArg(a);
	}
	
	public static OfflinePersona senderOrOfflinePersonaFromArg(Sender s, String a) {
		if("@s".equals(a)) a = s.getName();
		return offlinePersonaFromArg(a);
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
        if (a.startsWith("id:") || a.startsWith("#")) {
            String sid = a.startsWith("#")? a.substring(1) : a.substring(3);
            if (NumberUtils.isDigits(sid)) {
                return ArcheCore.getPersonaControls().getPersonaById(Integer.valueOf(sid));
            }
        }
        int c = StringUtils.countMatches(a, "@");
        String player;
        int id = -1;

        if (c == 1) {
            String[] s = a.split("@");
            player = s[0];
            try {
                id = Integer.parseInt(s[1]);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                return null;
            }
        } else if (c == 0) {
            player = a;
        } else return null;

        PersonaHandler hand = ArcheCore.getControls().getPersonaHandler();
        
        UUID uuid = null;
        
        if(player.length() == 36) {
        	try { uuid = UUID.fromString(player); }
        	catch(IllegalArgumentException e) { return null; }
        } else {
        	Player p = Bukkit.getPlayer(player);
        	if(p != null) uuid = p.getUniqueId();
        	else uuid = ArcheCore.getControls().getPlayerUUIDFromName(player);
        }
        
        return id < 0 || id >= ArcheCore.getControls().personaSlots() ?
        		hand.getOfflinePersona(uuid) :
        		hand.getOfflinePersona(uuid, id);
    }
	
	public static Persona currentPersonaFromArg(String a){
		Player p = Bukkit.getPlayer(a);
		if(p == null) return getPreloadedPersonaFromName(a);
		else return ArcheCore.getControls().getPersonaHandler().getPersona(p);
	}

	private static Persona getPreloadedPersonaFromName(String name) {
		PersonaHandler hand = ArcheCore.getControls().getPersonaHandler();
        for (Persona persona : hand.getPersonas().stream()
        		.filter(ArcheOfflinePersona::isLoaded)
        		.map(ArchePersona.class::cast)
        		.collect(Collectors.toList())) {
            if (persona == null) continue;
            else if (persona.getPlayerName().equals(name)) return persona;
            else break;
        }
		return null;
	}
}
