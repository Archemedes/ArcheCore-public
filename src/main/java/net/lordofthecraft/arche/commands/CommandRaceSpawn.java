package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Configure racial specific spawns.
 */
public class CommandRaceSpawn implements CommandExecutor {

    private final PersonaHandler handler;

    public CommandRaceSpawn(PersonaHandler handler) {
        this.handler = handler;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
        if (args.length == 0) {
            return help(sender, command);
        }
        if (args.length > 0) {
            if ("list".equalsIgnoreCase(args[0])) {
                sender.sendMessage(ChatColor.AQUA + "Printing racial spawns...");
                handler.getRacespawns().forEach((key, l) -> sender.sendMessage(ChatColor.GOLD + key.name() + ": X: " + l.getBlockX() + ", Y: " + l.getBlockY() + " Z: " + l.getBlockZ() + " World: " + l.getWorld().getName()));
                return true;
            } else if ("remove".equalsIgnoreCase(args[0])) {
                if (args.length > 1) {
                    Race r = findRace(StringUtils.join(args, ' ', 1, args.length));
                    if (r != null) {
                        sender.sendMessage(ChatColor.DARK_GREEN + "Successfully removed the racial spawn for " + r.name());
                        ArchePersonaHandler.getInstance().removeRaceSpawn(r);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Could not find the race " + StringUtils.join(args, ' ', 1, args.length));
                    }
                } else {
                    return help(sender, command);
                }
                return true;
            }
            int parseTo = (args.length > 4 && StringUtils.isNumeric(args[args.length - 1])) ? args.length - 4 : args.length;
            Race r = findRace(StringUtils.join(args, ' ', 0, parseTo));
            if (r != null) {
                Location l = null;
                if (args.length < 3 && !(sender instanceof Player)) {
                    return help(sender, command);
                } else if (args.length > 3
                        && StringUtils.isNumeric(args[args.length - 1])
                        && StringUtils.isNumeric(args[args.length - 2])
                        && StringUtils.isNumeric(args[args.length - 3])
                        && Bukkit.getWorld(args[args.length - 4]) != null) {
                    l = new Location(Bukkit.getWorld(args[args.length - 4]), Integer.valueOf(args[args.length - 3]), Integer.valueOf(args[args.length - 2]), Integer.valueOf(args[args.length - 1]));
                } else if (sender instanceof Player) {
                    l = ((Player) sender).getLocation();
                }
                if (l != null) {
                    sender.sendMessage(ChatColor.GOLD + "Successfully set the racial spawn of " + r.name());
                    boolean b = ArchePersonaHandler.getInstance().addRaceSpawn(r, l);
                    if (b) {
                        sender.sendMessage(ChatColor.GRAY + "The old spawn for this race has been overwritten.");
                    }
                    return true;
                }
            }
        }
        return help(sender, command);
    }

    private boolean help(CommandSender s, String command) {
        s.sendMessage(ChatColor.AQUA + "Usage: /" + command + (s instanceof Player ? " [race]" : "[race] [world] [x] [y] [z]"));
        String ss = "";
        StringBuilder sb = new StringBuilder();
        sb.append("Valid races: ");
        for (Race r : Race.values()) {
            sb.append(ss);
            sb.append(r.name());
            ss = ", ";
        }
        s.sendMessage(sb.toString());
        s.sendMessage(ChatColor.AQUA + "This command allows you to assign a spawn location for an individual race.");
        s.sendMessage(ChatColor.DARK_GRAY + "A player will spawn here when they die or a new persona is made with this race (and teleport new personas is enabled).");
        s.sendMessage(ChatColor.DARK_GRAY + "The direction in which they spawn is decided by the direction you're facing when you run the command.");
        s.sendMessage(ChatColor.AQUA + "/" + command + " list will list all current spawns.");
        s.sendMessage(ChatColor.AQUA + "/" + command + " remove [race] will wipe the racial spawn of the race, defaulting it back to normal spawn.");
        return true;
    }

    private Race findRace(String s) {
        s = s.replace('_', ' ');
        for (Race r : Race.values()) {
            if (s.equalsIgnoreCase(r.getName())) return r;
        }
        return null;
    }
}
