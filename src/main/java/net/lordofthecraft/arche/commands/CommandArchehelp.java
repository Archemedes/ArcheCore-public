package net.lordofthecraft.arche.commands;

import net.lordofthecraft.arche.help.HelpDesk;
import net.lordofthecraft.arche.util.MessageUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

public class CommandArchehelp implements CommandExecutor {
	private final HelpDesk helpdesk;
	private final boolean overridden;
	
	private BaseComponent topics = null;
	private int knownTopics = 0;
	
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
                Set<String> tops = helpdesk.getTopics(sender);
                if(topics == null || knownTopics < tops.size()){
					knownTopics = 0;
					topics = new TextComponent();
					tops.forEach(t -> {
						knownTopics++;
						topics.addExtra(" ");
						topics.addExtra(MessageUtil.ArcheHelpButton(t));
						topics.addExtra(" ");
					});
				}
				((Player) sender).spigot().sendMessage(topics);
			} else {
                sender.sendMessage(helpdesk.getTopics(sender).toString());
            }
			sender.sendMessage(ChatColor.AQUA + "View help on a Topic with: " + ChatColor.ITALIC +  "/" + cmd + " [topic]");
		}else {
			String topic = StringUtils.join(args, ' ');
			if(sender instanceof Player){
                if (!helpdesk.outputHelp(topic, (Player) sender)) {
                    sender.sendMessage(ChatColor.RED + "Error: You do not have permission to view this topic.");
                }
            }else{
				String help = helpdesk.getHelpText(topic);
				if(help == null) sender.sendMessage(ChatColor.RED + "No such Help Topic: " + ChatColor.GRAY + topic);
                else {
                    sender.sendMessage(help);
                }
            }
		}
		
		return true;
	 }
}
