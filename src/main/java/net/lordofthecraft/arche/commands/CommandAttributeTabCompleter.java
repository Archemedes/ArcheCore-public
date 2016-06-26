package net.lordofthecraft.arche.commands;

import com.google.common.collect.Lists;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

/**
 * Created by Sean on 6/26/2016.
 */
public class CommandAttributeTabCompleter implements TabCompleter {

    public CommandAttributeTabCompleter() {
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String command, String[] args) {
        if (args.length == 2 && cmd.getName().equals("attribute")) {
            List<String> attrs = Lists.newArrayList();
            Lists.newArrayList(Attribute.values()).stream().forEach(at -> attrs.add(at.name()));
            return attrs;
        }
        return null;
    }
}
