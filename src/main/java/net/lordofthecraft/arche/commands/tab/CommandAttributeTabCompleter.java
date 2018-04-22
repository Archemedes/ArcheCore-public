package net.lordofthecraft.arche.commands.tab;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import net.lordofthecraft.arche.attributes.AttributeRegistry;

/**
 * Created by Sean on 6/26/2016.
 */
public class CommandAttributeTabCompleter implements TabCompleter {

    public CommandAttributeTabCompleter() {
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String command, String[] args) {
        if (args.length == 2 && cmd.getName().equals("attribute")) {
            String partial = args[1];
            
            return AttributeRegistry.getInstance().getAttributes().keySet().stream()
            	.filter(a->a.toUpperCase().startsWith(partial.toUpperCase()))
            	.map(s->s.replace(' ', '_'))
            	.collect(Collectors.toList());
        }
        return null;
    }
}
