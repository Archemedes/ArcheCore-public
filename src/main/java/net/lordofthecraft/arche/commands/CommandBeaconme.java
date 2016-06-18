package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.ArcheBeacon;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBeaconme implements CommandExecutor {

	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player){
			Player p = (Player) sender;
			ArcheBeacon.openBeacon(p);
		}else{
			sender.sendMessage("Command may only be executed by players.");
		}
		
		return true;
	}
}
