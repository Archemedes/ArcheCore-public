package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.interfaces.Economy;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.util.CommandUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMoney implements CommandExecutor {
	private final static double PAYMENT_PROXIMITY = 8;
	private final HelpDesk helpdesk;
	private final Economy econ;
	
	public CommandMoney(HelpDesk helpdesk, Economy economy){
		this.helpdesk = helpdesk;
		econ = economy;
		
		if(econ != null){
			String i = ChatColor.DARK_GREEN + "" + ChatColor.ITALIC;
			String a = ChatColor.AQUA+ "";
			String output = ChatColor.DARK_AQUA +""+ ChatColor.BOLD + "Using the command: " + i + "/money (or /mn)\n"
					+ i + "$/mn help$: " + a + "Show this help file\n"
					+ i + "$/mn$: " + a + "Display the amount of " + econ.currencyNamePlural() +   " your Persona has.\n"
					+ i + "$/mn top$: " + a + "Show the people with the most " + econ.currencyNamePlural() +"\n"
					+ i + "$/mn [player]$: " + a + "See how much " + econ.currencyNamePlural() +   " [player]'s Persona has.\n"
					+ i + "$/mn pay [player] [amt]$: " + a + "Pay [amt] to [player]'s current Persona, provided that they are nearby.\n";
					
			helpdesk.addInfoTopic("Money Command", output);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(econ == null){
			sender.sendMessage("ArcheCore economy functionality has been disabled from config.");
			return true;
		}
		
		if(args.length == 0){
			if(sender instanceof Player){
				Persona p = ArcheCore.getControls().getPersonaHandler().getPersona((Player) sender);
				sender.sendMessage(displayMoney(p));
			}else sendHelp(sender);
			return true;
		}else if (args[0].equalsIgnoreCase("help")) {
			sendHelp(sender);
			return true;
        } else if (args[0].equalsIgnoreCase("pay")) {
            if (args.length >= 3) {
                if (!(sender instanceof Player)) {
                    sendHelp(sender);
                    return true;
                }

                Player p = (Player) sender;
                double amt;
                try {
                    amt = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    return false;
                }
                if (amt < 0.01) return false;

                amt = (double) ((int) (amt * 10)) / 10d;

                Persona from = ArcheCore.getControls().getPersonaHandler().getPersona(p);
                Persona to = CommandUtil.currentPersonaFromArg(args[1]);


                if (from == null || to == null || to.getPlayer() == null)
                    sender.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "You both need a valid Persona");
                else if (econ.requirePaymentProximity() && (p.getWorld() != to.getPlayer().getWorld() || p.getLocation().distance(to.getPlayer().getLocation()) > PAYMENT_PROXIMITY))
                    sender.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "You must be near the Persona you wish to pay.");
                else if (!econ.has(from, amt))
                    sender.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "You do not have this amount of " + econ.currencyNamePlural());
                else {
                    Player pt = to.getPlayer();
                    String p1 = ChatColor.WHITE + from.getName() + ChatColor.GRAY + "" + ChatColor.ITALIC + " (" + from.getPlayerName() + ") " + ChatColor.AQUA;
                    String p2 = ChatColor.WHITE + to.getName() + ChatColor.GRAY + "" + ChatColor.ITALIC + " (" + to.getPlayerName() + ") ";
                    String mn = ChatColor.GOLD + "" + amt + " " + ChatColor.AQUA + (amt >= 1 && amt < 2 ? econ.currencyNameSingular() : econ.currencyNamePlural());
                    sender.sendMessage(ChatColor.AQUA + "You have paid " + p2 + mn);
                    if (pt != null) pt.sendMessage(p1 + "has given you " + mn);

                    econ.withdrawPersona(from, amt);
                    econ.depositPersona(to, amt);
                }
            } else {
                sendHelp(sender);
            }

			return true;
		} else if(isMod(sender) && args.length == 3 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("grand") || args[0].equalsIgnoreCase("grant"))){
			
			double amt;
			try{amt = Double.parseDouble(args[2]);} catch(NumberFormatException e){return false;}
			if(amt < 0) return false;
			
			amt = (double)((int)(amt * 10)) / 10d;
			
			Persona lookup = CommandUtil.personaFromArg(args[1]);
			if(lookup == null) sender.sendMessage(ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "No valid Persona found");
			else {
				if(args[0].equalsIgnoreCase("set"))
					econ.setPersona(lookup, amt);
				else
					econ.depositPersona(lookup, amt);
				sender.sendMessage("Successfully altered balance.");
			}
			
			return true;
		} else { //Assume args[0] is a player name. Request the money :)
            if (!args[0].equalsIgnoreCase("help") && !args[0].equalsIgnoreCase("pay")) {
                if (sender.hasPermission("archecore.money.seeother")) {
                    Persona lookup = CommandUtil.personaFromArg(args[0]);
                    sender.sendMessage(displayMoney(lookup));
                    if (lookup == null && (sender instanceof Player)) sendHelp(sender);
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to do this.");
                }
            }
			return true;
			
		}
	}
	
	private void sendHelp(CommandSender sender){
		if(sender instanceof Player) helpdesk.outputHelp("money command", (Player) sender);
		else sender.sendMessage(helpdesk.getHelpText("money command"));
		
		if(isMod(sender)){
			sender.sendMessage(ChatColor.DARK_AQUA + "[M] /mn set [player] [amt]: sets a Persona's money to this new number.");
			sender.sendMessage(ChatColor.DARK_AQUA + "[M] /mn grant [player] [amt]: add to a Persona's money by this amount.");
			sender.sendMessage(ChatColor.DARK_AQUA + "[M] /mn set [player] [amt]: set a Persona's money to this amount.");
			sender.sendMessage(ChatColor.DARK_AQUA + "[M] You can use [player]@[personaid] to modify a different Persona");
		}
		
	}
	
	private String displayMoney(Persona p){
		if(p == null) return ChatColor.RED + "ERROR: " + ChatColor.DARK_RED + "No valid Persona found";
		double money = econ.getBalance(p);
		money = (double)((int)(money * 10)) / 10d;
		
		return ChatColor.AQUA + p.getName() + ChatColor.DARK_GRAY + "" + ChatColor.ITALIC + " (" + p.getPlayerName() + ") " 
				+ ChatColor.AQUA + "currently has " + ChatColor.GOLD + money + " " + ChatColor.AQUA
                + (money >= 1 && money < 2 ? econ.currencyNameSingular() : econ.currencyNamePlural()
                + ChatColor.DARK_GRAY + "\n(Use /money help to see more information)");
    }
	
	private boolean isMod(CommandSender sender){
		return sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.economy");
	}
	
}
