package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.interfaces.*;
import org.bukkit.command.*;

public class CommandAutoage implements CommandExecutor
{
    private final PersonaHandler handler;
    
    public CommandAutoage(final IArcheCore plugin) {
        super();
        this.handler = plugin.getPersonaHandler();
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        this.handler.ageUs();
        sender.sendMessage("All eligible personas increased by 1 in age.");
        return true;
    }
}
