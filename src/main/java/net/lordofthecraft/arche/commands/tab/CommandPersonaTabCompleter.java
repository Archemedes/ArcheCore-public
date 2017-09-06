package net.lordofthecraft.arche.commands.tab;

import net.lordofthecraft.arche.commands.CommandPersona;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class CommandPersonaTabCompleter implements TabCompleter {

    public CommandPersonaTabCompleter() {
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("persona")) {
            if (args.length == 1) {
                return CommandPersona.PersonaCommand.getCommandsFromPartial(args[0], commandSender);
            } else if (args.length == 0) {
                return CommandPersona.PersonaCommand.getAllCommands(commandSender);
            }
        }
        return null;
    }
}
