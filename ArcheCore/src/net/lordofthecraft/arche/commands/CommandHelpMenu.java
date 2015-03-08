package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.help.HelpDesk;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHelpMenu implements CommandExecutor {
	private final HelpDesk helpdesk;
	
	public CommandHelpMenu(HelpDesk helpdesk){
		this.helpdesk = helpdesk;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player){
			Player p = (Player) sender;
			helpdesk.openHelpMenu(p);
		}else{
			sender.sendMessage("Command may only be executed by players.");
		}
		
		return true;
	}

}
