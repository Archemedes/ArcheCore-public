package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.interfaces.IArcheCore;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandRealname implements CommandExecutor{
	private final ArchePersonaHandler handler;
	
	public CommandRealname(IArcheCore plugin){
		this.handler = (ArchePersonaHandler) plugin.getPersonaHandler();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length > 0){
			String toFind = StringUtils.join(args, ' ', 0, args.length).toLowerCase();
			sender.sendMessage(ChatColor.GOLD + "Showing matches to Persona name " + ChatColor.GRAY + "" + ChatColor.ITALIC + toFind);
			for(ArchePersona[] ps : handler.getPersonas()){
				if(ps == null) continue;
				for(ArchePersona p : ps){
					if(p == null) continue;
					if(p.getName().toLowerCase().contains(toFind)){
						sender.sendMessage(ChatColor.ITALIC + p.getName() + ChatColor.GOLD + " is a Persona of " + ChatColor.RESET + "" + ChatColor.ITALIC + p.getPlayerName());
					}
				}
			}
			
			return true;
		}
		return false;
	}
	
}
