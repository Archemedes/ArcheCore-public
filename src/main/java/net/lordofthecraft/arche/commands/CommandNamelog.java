package net.lordofthecraft.arche.commands;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.util.CommandUtil;

public class CommandNamelog implements CommandExecutor {

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		
		if(args.length > 0){
			Persona persona = CommandUtil.personaFromArg(args[0]);
			
			if(persona == null) {
				sender.sendMessage(ChatColor.RED + "Error: Persona not found");
			} else {
				Set<String> pastNames = persona.getPastNames();
				sender.sendMessage(ChatColor.AQUA + "Showing all the used names of "
						+ ChatColor.RESET + persona.identify());
				sender.sendMessage(StringUtils.join(pastNames, ", "));
			}
				
			return true;
		}
		
		return false;
	}
	
}
