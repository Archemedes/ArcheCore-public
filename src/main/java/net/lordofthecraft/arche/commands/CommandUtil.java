package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

@SuppressWarnings("deprecation")
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
		
		OfflinePlayer p = Bukkit.getPlayer(player);
		if(p == null) p = Bukkit.getOfflinePlayer(player);
		if(p == null) return null;
		
		PersonaHandler hand = ArcheCore.getControls().getPersonaHandler();
		return id < 0 || id > 3? hand.getPersona(p) : hand.getAllPersonas(p)[id];
	}
	
	public static Persona currentPersonaFromArg(String a){
		OfflinePlayer p = Bukkit.getPlayer(a);
		if(p == null) p = Bukkit.getOfflinePlayer(a);
		if(p == null) return null;
		
		return ArcheCore.getControls().getPersonaHandler().getPersona(p);
	}
}
