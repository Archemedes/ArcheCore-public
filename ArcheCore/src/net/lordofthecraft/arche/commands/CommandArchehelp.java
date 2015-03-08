package net.lordofthecraft.arche.commands;

import java.util.Set;

import net.lordofthecraft.arche.enums.ChatBoxAction;
import net.lordofthecraft.arche.help.ArcheMessage;
import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.interfaces.ChatMessage;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandArchehelp implements CommandExecutor {
	private final HelpDesk helpdesk;
	private final boolean overridden;
	
	private ChatMessage topics = null;
	
	public CommandArchehelp(HelpDesk helpdesk, boolean overridden){
		this.helpdesk = helpdesk;
		this.overridden = overridden;
	}
	
	 @Override
	 public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		 
		if(args.length == 0){
			String cmd = overridden? "help" : "archehelp";
			
			sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "Available Help Topics (click to view):");
			
			if(sender instanceof Player){
				Set<String> tops = helpdesk.getTopics();
				if(topics == null || topics.size() < tops.size()){
					topics = new ArcheMessage("");
					for(String x : tops){
						topics.addLine(x)
							//.setUnderlined()
							.setHoverEvent(ChatBoxAction.SHOW_TEXT, "Click for help")
							.setClickEvent(ChatBoxAction.RUN_COMMAND, "/archehelp " + x)
							.addLine(", ");
					}
				}
				topics.sendTo((Player) sender);
			} else {
				sender.sendMessage(helpdesk.getTopics().toString());
			}
			sender.sendMessage(ChatColor.AQUA + "View help on a Topic with: " + ChatColor.ITALIC +  "/" + cmd + " [topic]");
			sender.sendMessage(ChatColor.AQUA + "Find info on Skills with: " + ChatColor.ITALIC +  "/skill [skill]");
		}else {
			String topic = StringUtils.join(args, ' ');
			if(sender instanceof Player){
				helpdesk.outputHelp(topic, (Player) sender);
			}else{
				String help = helpdesk.getHelpText(topic);
				if(help == null) sender.sendMessage(ChatColor.RED + "No such Help Topic: " + ChatColor.GRAY + topic);
				else sender.sendMessage(help);
			}
		}
		
		return true;
	 }
}
