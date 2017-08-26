package net.lordofthecraft.arche.help;

import net.md_5.bungee.api.chat.BaseComponent;

import org.bukkit.entity.Player;

/**
 * Help file with clickable links to other topics
 */
public class LinkedHelpFile extends HelpFile {
	private final BaseComponent[] messages;	
	
	public LinkedHelpFile(String topic, String text) {
		super(topic);
		
		messages = parseMultiple(text);
	}

	@Override
	public void output(Player p) {
		for(BaseComponent message : messages)
			p.spigot().sendMessage(message);
	}
	
	public String outputHelp(){
		StringBuilder builder = new StringBuilder(256);
		String prefix = "";
		for(BaseComponent message : messages){
			builder.append(prefix).append(message.toPlainText());
			prefix = "\n";
		}
		
		return builder.toString();
	}
}
