package net.lordofthecraft.arche.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.seasons.LotcianCalendar;

public class CommandDate implements CommandExecutor
{
    private final LotcianCalendar calendar;
    
    public CommandDate(ArcheCore plugin) {
        calendar = plugin.getCalendar();
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("real"))
        	sender.sendMessage(this.calendar.toString());
        else
        	sender.sendMessage(this.calendar.toPrettyString());
        
        return true;
    }
}

