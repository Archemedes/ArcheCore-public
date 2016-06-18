package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.interfaces.IArcheCore;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandAutoage implements CommandExecutor {
	private final PersonaHandler handler;
	
	public CommandAutoage(IArcheCore plugin){
		this.handler = plugin.getPersonaHandler();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		handler.ageUs();
		sender.sendMessage("All eligible personas increased by 1 in age.");
		return true;
	}
	
	
}
