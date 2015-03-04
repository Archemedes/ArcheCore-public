package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.interfaces.*;
import org.bukkit.command.*;
import org.apache.commons.lang.*;
import org.bukkit.*;
import net.lordofthecraft.arche.persona.*;
import java.util.*;

public class CommandRealname implements CommandExecutor
{
    private final ArchePersonaHandler handler;
    
    public CommandRealname(final IArcheCore plugin) {
        super();
        this.handler = (ArchePersonaHandler)plugin.getPersonaHandler();
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length > 0) {
            final String toFind = StringUtils.join((Object[])args, ' ', 0, args.length).toLowerCase();
            sender.sendMessage(ChatColor.GOLD + "Showing matches to Persona name " + ChatColor.GRAY + "" + ChatColor.ITALIC + toFind);
            for (final ArchePersona[] ps : this.handler.getPersonas()) {
                if (ps == null) {
                    continue;
                }
                for (final ArchePersona p : ps) {
                    if (p != null) {
                        if (p.getName().toLowerCase().contains(toFind)) {
                            sender.sendMessage(ChatColor.ITALIC + p.getName() + ChatColor.GOLD + " is a Persona of " + ChatColor.RESET + "" + ChatColor.ITALIC + p.getPlayerName());
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }
}
