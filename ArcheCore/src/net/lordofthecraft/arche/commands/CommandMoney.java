package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.help.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import net.lordofthecraft.arche.*;
import net.lordofthecraft.arche.interfaces.*;

public class CommandMoney implements CommandExecutor
{
    private static final double PAYMENT_PROXIMITY = 8.0;
    private final HelpDesk helpdesk;
    private final Economy econ;
    
    public CommandMoney(final HelpDesk helpdesk, final Economy economy) {
        super();
        this.helpdesk = helpdesk;
        this.econ = economy;
        if (this.econ != null) {
            final String i = ChatColor.DARK_GREEN + "" + ChatColor.ITALIC;
            final String a = ChatColor.AQUA + "";
            final String output = ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Using the command: " + i + "/money (or /mn)\n" + i + "$/mn help$: " + a + "Show this help file\n" + i + "$/mn $: " + a + "Display the amount of " + this.econ.currencyNamePlural() + " your Persona has.\n" + i + "$/mn [player]$: " + a + "See how much " + this.econ.currencyNamePlural() + " [player]'s Persona has.\n" + i + "$/mn pay [player] [amt]$: " + a + "Pay [amt] to [player]'s current Persona, provided that they are nearby.\n";
            helpdesk.addInfoTopic("Money Command", output);
        }
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (this.econ == null) {
            sender.sendMessage("ArcheCore economy functionality has been disabled from config.");
            return true;
        }
        if (args.length == 0) {
            if (sender instanceof Player) {
                final Persona p = ArcheCore.getControls().getPersonaHandler().getPersona((Player)sender);
                sender.sendMessage(this.displayMoney(p));
            }
            else {
                this.sendHelp(sender);
            }
            return true;
        }
        if (!args[0].equalsIgnoreCase("pay") || args.length != 3) {
            Label_0933: {
                if (this.isMod(sender) && args.length == 3) {
                    if (!args[0].equalsIgnoreCase("set") && !args[0].equalsIgnoreCase("grand")) {
                        if (!args[0].equalsIgnoreCase("grant")) {
                            break Label_0933;
                        }
                    }
                    double amt;
                    try {
                        amt = Double.parseDouble(args[2]);
                    }
                    catch (NumberFormatException e) {
                        return false;
                    }
                    if (amt < 0.0) {
                        return false;
                    }
                    amt = (int)(amt * 10.0) / 10.0;
                    final Persona lookup = CommandUtil.personaFromArg(args[1]);
                    if (lookup == null) {
                        sender.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "No valid Persona found");
                    }
                    else {
                        if (args[0].equalsIgnoreCase("set")) {
                            this.econ.setPersona(lookup, amt);
                        }
                        else {
                            this.econ.depositPersona(lookup, amt);
                        }
                        sender.sendMessage("Successfully altered balance.");
                    }
                    return true;
                }
            }
            if (sender.hasPermission("archecore.money.seeother")) {
                final Persona lookup2 = CommandUtil.personaFromArg(args[0]);
                sender.sendMessage(this.displayMoney(lookup2));
                if (lookup2 == null && sender instanceof Player) {
                    this.sendHelp(sender);
                }
            }
            else {
                sender.sendMessage(ChatColor.RED + "You do not have permission to do this.");
            }
            return true;
        }
        if (!(sender instanceof Player)) {
            this.sendHelp(sender);
            return true;
        }
        final Player p2 = (Player)sender;
        double amt2;
        try {
            amt2 = Double.parseDouble(args[2]);
        }
        catch (NumberFormatException e2) {
            return false;
        }
        if (amt2 < 0.01) {
            return false;
        }
        amt2 = (int)(amt2 * 10.0) / 10.0;
        final Persona from = ArcheCore.getControls().getPersonaHandler().getPersona(p2);
        final Persona to = CommandUtil.currentPersonaFromArg(args[1]);
        if (from == null || to == null || to.getPlayer() == null) {
            sender.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "You both need a valid Persona");
        }
        else if (this.econ.requirePaymentProximity() && (p2.getWorld() != to.getPlayer().getWorld() || p2.getLocation().distance(to.getPlayer().getLocation()) > 8.0)) {
            sender.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "You must be near the Persona you wish to pay.");
        }
        else if (!this.econ.has(from, amt2)) {
            sender.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "You do not have this amount of " + this.econ.currencyNamePlural());
        }
        else {
            final Player pt = to.getPlayer();
            final String p3 = ChatColor.WHITE + from.getName() + ChatColor.GRAY + "" + ChatColor.ITALIC + " (" + from.getPlayerName() + ") " + ChatColor.AQUA;
            final String p4 = ChatColor.WHITE + to.getName() + ChatColor.GRAY + "" + ChatColor.ITALIC + " (" + to.getPlayerName() + ") ";
            final String mn = ChatColor.GOLD + "" + amt2 + " " + ChatColor.AQUA + ((amt2 >= 1.0 && amt2 < 2.0) ? this.econ.currencyNameSingular() : this.econ.currencyNamePlural());
            sender.sendMessage(ChatColor.AQUA + "You have paid " + p4 + mn);
            if (pt != null) {
                pt.sendMessage(p3 + "has given you " + mn);
            }
            this.econ.withdrawPersona(from, amt2);
            this.econ.depositPersona(to, amt2);
        }
        return true;
    }
    
    private void sendHelp(final CommandSender sender) {
        if (sender instanceof Player) {
            this.helpdesk.outputHelp("money command", (Player)sender);
        }
        else {
            sender.sendMessage(this.helpdesk.getHelpText("money command"));
        }
        if (this.isMod(sender)) {
            sender.sendMessage(ChatColor.DARK_AQUA + "[M] /mn set [player] [amt]: sets a Persona's money to this new number.");
            sender.sendMessage(ChatColor.DARK_AQUA + "[M] /mn grant [player] [amt]: add to a Persona's money by this amount.");
            sender.sendMessage(ChatColor.DARK_AQUA + "[M] /mn set [player] [amt]: set a Persona's money to this amount.");
            sender.sendMessage(ChatColor.DARK_AQUA + "[M] You can use [player]@[personaid] to modify a different Persona");
        }
    }
    
    private String displayMoney(final Persona p) {
        if (p == null) {
            return ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "No valid Persona found";
        }
        double money = this.econ.getBalance(p);
        money = (int)(money * 10.0) / 10.0;
        return ChatColor.AQUA + p.getName() + ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + " (" + p.getPlayerName() + ") " + ChatColor.AQUA + "currently has " + ChatColor.GOLD + money + " " + ChatColor.AQUA + ((money >= 1.0 && money < 2.0) ? this.econ.currencyNameSingular() : this.econ.currencyNamePlural());
    }
    
    private boolean isMod(final CommandSender sender) {
        return sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.economy");
    }
}
