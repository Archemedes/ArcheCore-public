package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.help.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

public class CommandHelpMenu implements CommandExecutor
{
    private final HelpDesk helpdesk;
    
    public CommandHelpMenu(final HelpDesk helpdesk) {
        super();
        this.helpdesk = helpdesk;
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player)sender;
            this.helpdesk.openHelpMenu(p);
        }
        else {
            sender.sendMessage("Command may only be executed by players.");
        }
        return true;
    }
}
