package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.help.*;
import net.lordofthecraft.arche.persona.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import org.apache.commons.lang.*;
import net.lordofthecraft.arche.save.*;
import net.lordofthecraft.arche.save.tasks.*;
import net.lordofthecraft.arche.interfaces.*;
import java.util.*;

public class CommandPersona implements CommandExecutor
{
    private final HelpDesk helpdesk;
    private final ArchePersonaHandler handler;
    private final int delay;
    private final boolean prefix;
    
    public CommandPersona(final HelpDesk helpdesk, final ArchePersonaHandler handler, final int delay, final boolean prefix) {
        super();
        this.helpdesk = helpdesk;
        this.handler = handler;
        this.delay = delay;
        this.prefix = prefix;
        final String i = ChatColor.BLUE + "" + ChatColor.ITALIC;
        final String a = ChatColor.AQUA + "";
        final String output = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "How to use the command: " + i + "/persona\n" + ChatColor.BLUE + "Type " + i + "/persona [par]" + ChatColor.BLUE + " where " + i + "'[par]'" + ChatColor.BLUE + " is any of the following:\n" + a + i + "$</persona view >view {player}$: " + a + "View the current Character Card of {Player}.\n" + i + "$</persona name >name [new name]$: " + a + "Rename your Persona to the given name.\n" + (prefix ? (i + "$</persona prefix >prefix [prefix]$: " + a + "Sets Persona Prefix (delete with $</persona clearprefix>clearprefix$).\n") : "") + i + "$</persona age >age [new age]$: " + a + "Set your character's age.\n" + i + "$</persona autoage>autoage$: " + a + "Toggle automatic aging for this persona.\n" + i + "$</persona addbio >addbio$: " + a + "Add a line of text to your Persona's bio!.\n" + i + "$</persona clearbio>clearbio$: " + a + "Clear your Persona's  bio completely.\n";
        helpdesk.addInfoTopic("Persona Command", output);
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            if (sender instanceof Player) {
                this.helpdesk.outputHelp("persona command", (Player)sender);
            }
            else {
                sender.sendMessage(this.helpdesk.getHelpText("persona command"));
            }
            if (sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.persona")) {
                sender.sendMessage(ChatColor.DARK_AQUA + "[M] Force a permakill with 'permakill [persona]'. Default on your current Persona");
                sender.sendMessage(ChatColor.DARK_AQUA + "[M] Change Apparant Race with 'setrace'. This changes visible race, but not the underlying race.");
                sender.sendMessage(ChatColor.DARK_AQUA + "[M] You can add the flag '-p {player}' to the end of the command to modify someone's current Persona.");
                sender.sendMessage(ChatColor.DARK_AQUA + "[M] You can use [player]@[personaid] to modify a different Persona");
            }
            return true;
        }
        Persona pers = null;
        if ((args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("permakill")) && args.length > 1) {
            pers = CommandUtil.personaFromArg(args[1]);
        }
        else if (args.length > 3 && args[args.length - 2].equals("-p") && (sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.persona"))) {
            pers = CommandUtil.personaFromArg(args[args.length - 1]);
        }
        else if (sender instanceof Player) {
            pers = this.handler.getPersona((Player)sender);
        }
        if (pers == null) {
            sender.sendMessage(ChatColor.RED + "Error: No persona found to modify");
            return true;
        }
        if (args[0].equalsIgnoreCase("view")) {
            final Player t = Bukkit.getPlayer(pers.getPlayerUUID());
            if (t != null && !this.handler.mayUse(t)) {
                sender.sendMessage(ChatColor.DARK_AQUA + "This player is a Wandering Soul (may not use Personas)");
            }
            else {
                for (final String x : this.handler.whois(pers)) {
                    sender.sendMessage(x);
                }
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("autoage")) {
            final boolean auto = pers.doesAutoAge();
            if (auto && !sender.hasPermission("archecore.stopautoage")) {
                sender.sendMessage(ChatColor.RED + "Error: You may not turn off Auto-aging once on.");
            }
            else {
                sender.sendMessage(ChatColor.AQUA + "Turned " + ChatColor.GOLD + "" + ChatColor.BOLD + (auto ? "OFF" : "ON") + ChatColor.AQUA + " auto aging.");
                pers.setAutoAge(!auto);
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("clearprefix") && this.prefix) {
            pers.clearPrefix();
            sender.sendMessage(ChatColor.AQUA + "Persona prefix was cleared.");
            return true;
        }
        if (args[0].equalsIgnoreCase("clearbio")) {
            pers.clearDescription();
            sender.sendMessage(ChatColor.AQUA + "Cleared your Bio!");
            return true;
        }
        if (args[0].equalsIgnoreCase("permakill")) {
            if (!sender.hasPermission("archecore.admin") && !sender.hasPermission("archecore.mod.persona")) {
                sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
            }
            else {
                final Player other = pers.getPlayer();
                if (other == null) {
                    sender.sendMessage(ChatColor.RED + "Too risky to remove personas of offline players");
                    return true;
                }
                if (pers.remove()) {
                    if (other != null) {
                        if (this.handler.countPersonas(other) == 0 && !other.hasPermission("archecore.exempt")) {
                            other.kickPlayer("Your final Persona was Permakilled. Please relog.");
                        }
                        else {
                            other.sendMessage(ChatColor.DARK_GRAY + "A persona of yours was Permakilled: " + pers.getName());
                        }
                    }
                    sender.sendMessage(ChatColor.AQUA + "You have permakilled Persona " + ChatColor.WHITE + pers.getName() + ChatColor.AQUA + " belonging to " + ChatColor.WHITE + pers.getPlayerName());
                }
                else {
                    sender.sendMessage(ChatColor.RED + "I'm afraid I can't do that.");
                }
            }
            return true;
        }
        if (args.length > 1) {
            if (args[0].equalsIgnoreCase("name")) {
                final int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? (args.length - 2) : args.length;
                final String name = StringUtils.join((Object[])args, ' ', 1, parseTo);
                final long timeLeft = pers.getRenamed() / 60000L - System.currentTimeMillis() / 60000L + this.delay;
                if (timeLeft > 0L && !sender.hasPermission("archecore.persona.quickrename")) {
                    sender.sendMessage(ChatColor.RED + "You must wait " + timeLeft + " minutes before renaming again");
                }
                else if (name.length() <= 32 || sender.hasPermission("archecore.persona.longname")) {
                    pers.setName(name);
                    sender.sendMessage(ChatColor.AQUA + "Persona name was set to: " + ChatColor.RESET + name);
                    if (sender == pers.getPlayer()) {
                        SaveHandler.getInstance().put(new PersonaRenameTask(pers));
                    }
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Error: Name too long. Max length 32 characters");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("prefix") && this.prefix) {
                final int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? (args.length - 2) : args.length;
                final String name = StringUtils.join((Object[])args, ' ', 1, parseTo);
                if (name.length() <= 16) {
                    pers.setPrefix(name);
                    sender.sendMessage(ChatColor.AQUA + "Persona prefix was set to: " + ChatColor.RESET + name);
                }
                else {
                    sender.sendMessage(ChatColor.RED + "Error: Prefix too long. Max length 16 characters");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("addbio")) {
                final int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? (args.length - 2) : args.length;
                final String line = StringUtils.join((Object[])args, ' ', 1, parseTo);
                int length = line.length();
                if (pers.getDescription() != null) {
                    length += pers.getDescription().length();
                }
                if (length > 150 && !sender.hasPermission("archecore.persona.longbio")) {
                    sender.sendMessage(ChatColor.RED + "Error: Biography too long.");
                }
                else {
                    pers.addDescription(line);
                    sender.sendMessage(ChatColor.AQUA + "Added: " + ChatColor.RESET + line);
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("age")) {
                if (StringUtils.isNumeric(args[1])) {
                    final int ageNow = pers.getAge();
                    final int age = Integer.parseInt(args[1]);
                    if (sender.hasPermission("archecore.ageless")) {
                        pers.setAge(age);
                        sender.sendMessage(ChatColor.AQUA + "Set your Persona's age to: " + ChatColor.RESET + age);
                    }
                    else if (ageNow > age) {
                        sender.sendMessage(ChatColor.RED + "Error: You cannot become younger");
                    }
                    else if (age < 5 || age > pers.getRace().getMaximumAge()) {
                        sender.sendMessage(ChatColor.RED + "Error: Age must be between 5 and " + pers.getRace().getMaximumAge());
                    }
                    else {
                        pers.setAge(age);
                        sender.sendMessage(ChatColor.AQUA + "Set your Persona's age to: " + ChatColor.RESET + age);
                    }
                    return true;
                }
            }
            else {
                if (args[0].equalsIgnoreCase("setbio")) {
                    final int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? (args.length - 2) : args.length;
                    final String line = StringUtils.join((Object[])args, ' ', 1, parseTo);
                    pers.setDescription(line);
                    sender.sendMessage(ChatColor.AQUA + "Bio now reads: " + line);
                    return true;
                }
                if (args[0].equalsIgnoreCase("setrace")) {
                    if (!sender.hasPermission("archecore.admin") && !sender.hasPermission("archecore.mod.persona") && !sender.hasPermission("archecore.persona.setrace")) {
                        sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
                    }
                    else {
                        final int parseTo = (args.length > 3 && args[args.length - 2].equals("-p")) ? (args.length - 2) : args.length;
                        final String race = StringUtils.join((Object[])args, ' ', 1, parseTo);
                        pers.setApparentRace(race);
                        sender.sendMessage(ChatColor.AQUA + "Set visible race of this persona to: " + ChatColor.RESET + race);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
