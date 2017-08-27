package net.lordofthecraft.arche.util;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;

public class CommandUtil {

	
	public static Persona personaFromArg(String a){
		int c = StringUtils.countMatches(a, "@");
		String player;
		int id = -1;
		
		if(c == 1){
			String[] s = a.split("@");
			player = s[0];
			try{id = Integer.parseInt(s[1]);} catch(NumberFormatException e){return null;}
		} else if (c == 0){
			player = a;
		} else return null;
		
		PersonaHandler hand = ArcheCore.getControls().getPersonaHandler();
		
		Player p = Bukkit.getPlayer(player);
		if(p == null) return getPreloadedPersonaFromName(player);
		else return id < 0 || id > 3? hand.getPersona(p) : hand.getAllPersonas(p)[id];
	}
	
	public static Persona currentPersonaFromArg(String a){
		Player p = Bukkit.getPlayer(a);
		if(p == null) return getPreloadedPersonaFromName(a);
		else return ArcheCore.getControls().getPersonaHandler().getPersona(p);
	}
	
	private static Persona getPreloadedPersonaFromName(String name) {
		PersonaHandler hand = ArcheCore.getControls().getPersonaHandler();
		for(Persona[] personas : hand.getPersonas()) {
			for(Persona persona : personas) {
				if(persona == null) continue;
				else if(persona.getPlayerName().equals(name)) return persona;
				else break;
			}
		}
		return null;
	}
}
