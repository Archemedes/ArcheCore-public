package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.interfaces.IArcheCore;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
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
            if (toFind.length() < 3) {
                sender.sendMessage(ChatColor.RED + "Error: Please enter a name longer than 3 characters");
                return true;
            }
            sender.sendMessage(ChatColor.GOLD + "Showing matches to Persona name " + ChatColor.GRAY + "" + ChatColor.ITALIC + toFind);
            for (OfflinePersona p : handler.getPersonas()) {
                if (p == null) continue;
                if (p.getName().toLowerCase().contains(toFind)) {
                    sender.sendMessage(ChatColor.ITALIC + p.getName() + ChatColor.GOLD + " is a Persona of " + ChatColor.RESET + "" + ChatColor.ITALIC + p.getPlayerName());
                }
            }

            return true;
		}
		return false;
	}
	
}
