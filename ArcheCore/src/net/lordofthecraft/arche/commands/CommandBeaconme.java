package net.lordofthecraft.arche.commands;

import org.bukkit.command.*;
import org.bukkit.entity.*;
import net.lordofthecraft.arche.*;

public class CommandBeaconme implements CommandExecutor
{
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (sender instanceof Player) {
            final Player p = (Player)sender;
            ArcheBeacon.openBeacon(p);
        }
        else {
            sender.sendMessage("Command may only be executed by players.");
        }
        return true;
    }
}
