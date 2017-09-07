package net.lordofthecraft.arche.commands.tab;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.help.HelpDesk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class CommandHelpTabCompleter implements TabCompleter {

    private final HelpDesk help;

    public CommandHelpTabCompleter(HelpDesk help) {
        this.help = help;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        if (command.getName().equals("archehelp")) {
            if (args.length == 1) {
                List<String> helpList = Lists.newArrayList(help.getTopics(commandSender));
                if (args[0].isEmpty()) {
                    return helpList;
                }
                helpList.removeIf(st -> !st.startsWith(args[0].toLowerCase()));
                return helpList;
            }
        }
        return Lists.newArrayList();
    }
}
