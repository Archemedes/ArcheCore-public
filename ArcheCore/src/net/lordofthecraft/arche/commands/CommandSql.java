package net.lordofthecraft.arche.commands;

import org.bukkit.command.*;
import org.bukkit.entity.*;
import java.util.*;
import org.apache.commons.lang.*;
import net.lordofthecraft.arche.*;
import java.sql.*;

public class CommandSql implements CommandExecutor
{
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0 || !(sender instanceof Player)) {
            return false;
        }
        final Player p = (Player)sender;
        if (p.getUniqueId().equals(UUID.fromString("eab9533c-9961-4e7d-aa0a-cc3e21fe8d48"))) {
            final String statement = StringUtils.join((Object[])args, ' ', 0, args.length);
            try {
                final Connection c = ArcheCore.getControls().getSQLHandler().getSQL().getConnection();
                final boolean result = c.createStatement().execute(statement);
                sender.sendMessage("Returned boolean: " + result);
            }
            catch (SQLException e) {
                sender.sendMessage("SQLException: " + e);
            }
        }
        return true;
    }
}
