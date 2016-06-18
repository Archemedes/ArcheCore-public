package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.ArcheCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandSqlClone implements CommandExecutor{
    public CommandSqlClone() {}

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Status of restart clone is "+ ArcheCore.getControls().isCloning());
            return true;
        }
        if (args[0].equalsIgnoreCase("true") || args[0].equalsIgnoreCase("false")) {
            ArcheCore.getControls().setShouldClone(args[0].equalsIgnoreCase("true"));
            sender.sendMessage("Cloning status set to "+ArcheCore.getControls().isCloning());
            return true;
        }
        return false;
    }
}
