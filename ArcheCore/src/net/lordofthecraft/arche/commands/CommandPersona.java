package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.PersonaRenameTask;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPersona implements CommandExecutor {
	private final HelpDesk helpdesk;
	private final ArchePersonaHandler handler;
	private final int delay;
	private final boolean prefix;
	
	public CommandPersona(HelpDesk helpdesk, ArchePersonaHandler handler, int delay, boolean prefix){
		this.helpdesk = helpdesk;
		this.handler = handler;
		this.delay = delay;
		this.prefix = prefix;
		
		String i =  ChatColor.BLUE+""+ ChatColor.ITALIC;
		String a = ChatColor.AQUA+ "";
		String output = ChatColor.DARK_AQUA +""+ ChatColor.BOLD + "How to use the command: " + i + "/persona\n"
				+ ChatColor.BLUE + "Type " + i + "/persona [par]" + ChatColor.BLUE + " where " + i + "'[par]'" + ChatColor.BLUE + " is any of the following:\n" + a
				+ i + "$</persona view >view {player}$: " + a + "View the current Character Card of {Player}.\n"
				+ i + "$</persona name >name [new name]$: " + a + "Rename your Persona to the given name.\n"
				+(prefix? (i + "$</persona prefix >prefix [prefix]$: " + a + "Sets Persona Prefix (delete with $</persona clearprefix>clearprefix$).\n"): "")
				+ i + "$</persona age >age [new age]$: " + a + "Set your character's age.\n"
				+ i + "$</persona autoage>autoage$: " + a + "Toggle automatic aging for this persona.\n"
				+ i + "$</persona addbio >addbio$: " + a + "Add a line of text to your Persona's bio!.\n"
				+ i + "$</persona clearbio>clearbio$: " + a + "Clear your Persona's  bio completely.\n";
		
		helpdesk.addInfoTopic("Persona Command", output);
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0 || args[0].equalsIgnoreCase("help")){
			if(sender instanceof Player) helpdesk.outputHelp("persona command", (Player) sender);
			else sender.sendMessage(helpdesk.getHelpText("persona command"));
			
			if(sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.persona")){
				sender.sendMessage(ChatColor.DARK_AQUA + "[M] Force a permakill with 'permakill [persona]'. Default on your current Persona");
				sender.sendMessage(ChatColor.DARK_AQUA + "[M] Change Apparant Race with 'setrace'. This changes visible race, but not the underlying race.");
				sender.sendMessage(ChatColor.DARK_AQUA + "[M] You can add the flag '-p {player}' to the end of the command to modify someone's current Persona.");
				sender.sendMessage(ChatColor.DARK_AQUA + "[M] You can use [player]@[personaid] to modify a different Persona");
			}
			return true;
		} else {
			
			//Go through process to find the Persona we want
			Persona pers = null;
			if( ( args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("permakill") )&& args.length > 1){
				pers = CommandUtil.personaFromArg(args[1]);
			} else if(args.length > 3 && args[args.length - 2].equals("-p") && (sender.hasPermission("archecore.admin") || sender.hasPermission("archecore.mod.persona")) ){
				pers = CommandUtil.personaFromArg(args[args.length - 1]);
			} else if (sender instanceof Player){
				pers = handler.getPersona((Player) sender);
			}
			
			if(pers == null){
				sender.sendMessage(ChatColor.RED + "Error: No persona found to modify");
				return true;
			}
			
			if(args[0].equalsIgnoreCase("view")){
				Player t = Bukkit.getPlayer(pers.getPlayerUUID());
				if(t != null && !handler.mayUse(t)){
					sender.sendMessage(ChatColor.DARK_AQUA + "This player is a Wandering Soul (may not use Personas)");
				}else{
					//If the persona is found the Whois should always succeed
					//We have assured the persona is found earlier
					for(String x : handler.whois(pers))
						sender.sendMessage(x);
				}	
				return true;
			} else if (args[0].equalsIgnoreCase("autoage")){
				boolean auto = pers.doesAutoAge();
				
				if(auto == true && !sender.hasPermission("archecore.stopautoage")){
					sender.sendMessage(ChatColor.RED + "Error: You may not turn off Auto-aging once on.");
				} else {
					sender.sendMessage(ChatColor.AQUA + "Turned " + ChatColor.GOLD +""+ ChatColor.BOLD + (auto? "OFF":"ON") + ChatColor.AQUA + " auto aging.");
					pers.setAutoAge(!auto);
				}
				
				return true;
			} else if (args[0].equalsIgnoreCase("clearprefix") && prefix){
				pers.clearPrefix();
				sender.sendMessage(ChatColor.AQUA + "Persona prefix was cleared.");
				return true;
			} else if (args[0].equalsIgnoreCase("clearbio")){
				pers.clearDescription();
				sender.sendMessage(ChatColor.AQUA + "Cleared your Bio!");
				return true;
			} else if (args[0].equalsIgnoreCase("permakill")){
				if(!sender.hasPermission("archecore.admin") && !sender.hasPermission("archecore.mod.persona")){
					sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
				} else {
					Player other = pers.getPlayer();
					
					//Only do if online else problems
					if(other == null){
						sender.sendMessage(ChatColor.RED + "Too risky to remove personas of offline players");
						return true;
					}
					
					if(pers.remove()){
						if(other != null){
							if(handler.countPersonas(other) == 0 && !other.hasPermission("archecore.exempt"))
								other.kickPlayer("Your final Persona was Permakilled. Please relog.");
							else other.sendMessage(ChatColor.DARK_GRAY + "A persona of yours was Permakilled: " + pers.getName());
						}
						
						sender.sendMessage(ChatColor.AQUA + "You have permakilled Persona " + ChatColor.WHITE + pers.getName() + ChatColor.AQUA + " belonging to " + ChatColor.WHITE + pers.getPlayerName() );
					} else sender.sendMessage(ChatColor.RED + "I'm afraid I can't do that.");
				}
				return true;
			}else if(args.length > 1){
				if(args[0].equalsIgnoreCase("name")){
					int parseTo = (args.length > 3 && args[args.length - 2].equals("-p"))? args.length - 2 : args.length;
					String name = StringUtils.join(args, ' ', 1, parseTo);
					
					long timeLeft = (pers.getRenamed() / 60000) - (System.currentTimeMillis() / 60000) + delay; 
					if(timeLeft > 0 && !sender.hasPermission("archecore.persona.quickrename")){
						sender.sendMessage(ChatColor.RED + "You must wait " + timeLeft + " minutes before renaming again");
					}else if(name.length() <= 32 || sender.hasPermission("archecore.persona.longname")){
						pers.setName(name);
						sender.sendMessage(ChatColor.AQUA + "Persona name was set to: " + ChatColor.RESET + name);
						if(sender == pers.getPlayer()) //Player renamed by his own accord
							SaveHandler.getInstance().put(new PersonaRenameTask(pers));
					} else { 
						sender.sendMessage(ChatColor.RED + "Error: Name too long. Max length 32 characters");
					}
					return true;
					
				} else if (args[0].equalsIgnoreCase("prefix") && prefix){
					int parseTo = (args.length > 3 && args[args.length - 2].equals("-p"))? args.length - 2 : args.length;
					String name = StringUtils.join(args, ' ', 1, parseTo);
					
					if(name.length() <= 16){
						pers.setPrefix(name);
						sender.sendMessage(ChatColor.AQUA + "Persona prefix was set to: " + ChatColor.RESET + name);
					} else {
						sender.sendMessage(ChatColor.RED + "Error: Prefix too long. Max length 16 characters");
					}
					return true;
				} else if (args[0].equalsIgnoreCase("addbio")){	
					int parseTo = (args.length > 3 && args[args.length - 2].equals("-p"))? args.length - 2 : args.length;
					String line = StringUtils.join(args, ' ', 1, parseTo);
					
					int length = line.length();
					if(pers.getDescription() != null) length += pers.getDescription().length();
					if(length > 150 && !sender.hasPermission("archecore.persona.longbio")){
						sender.sendMessage(ChatColor.RED + "Error: Biography too long.");
					}else{
						pers.addDescription(line);
						sender.sendMessage(ChatColor.AQUA + "Added: " + ChatColor.RESET + line);
					}
					
					return true;
				} else if (args[0].equalsIgnoreCase("age")){
					if(StringUtils.isNumeric(args[1])){
						int ageNow = pers.getAge();
						int age = Integer.parseInt(args[1]);
						
						
						if(sender.hasPermission("archecore.ageless")){
							pers.setAge(age);
							sender.sendMessage(ChatColor.AQUA + "Set your Persona's age to: " + ChatColor.RESET + age);
						}else if(ageNow > age){
							sender.sendMessage(ChatColor.RED + "Error: You cannot become younger");
						}else if( age < 5 || age > pers.getRace().getMaximumAge()){
							sender.sendMessage(ChatColor.RED + "Error: Age must be between 5 and " + pers.getRace().getMaximumAge());
						}else{
							pers.setAge(age);
							sender.sendMessage(ChatColor.AQUA + "Set your Persona's age to: " + ChatColor.RESET + age);
						}
						
						return true;
					}
				} else if (args[0].equalsIgnoreCase("setbio")){	
					int parseTo = (args.length > 3 && args[args.length - 2].equals("-p"))? args.length - 2 : args.length;
					String line = StringUtils.join(args, ' ', 1, parseTo);
					
					pers.setDescription(line);
					sender.sendMessage(ChatColor.AQUA + "Bio now reads: " + line);
					return true;
				} else if (args[0].equalsIgnoreCase("setrace")){
					if(!sender.hasPermission("archecore.admin") && !sender.hasPermission("archecore.mod.persona") && !sender.hasPermission("archecore.persona.setrace")){
						sender.sendMessage(ChatColor.RED + "Error: Permission denied.");
					} else {
						int parseTo = (args.length > 3 && args[args.length - 2].equals("-p"))? args.length - 2 : args.length;
						String race = StringUtils.join(args, ' ', 1, parseTo);
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
