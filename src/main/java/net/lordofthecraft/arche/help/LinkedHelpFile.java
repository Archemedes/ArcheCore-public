package net.lordofthecraft.arche.help;

import net.lordofthecraft.arche.interfaces.ChatMessage;
import org.bukkit.entity.Player;

/**
 * Help file with clickable links to other topics
 */
public class LinkedHelpFile extends HelpFile {
	private final ChatMessage[] messages;	
	
	public LinkedHelpFile(String topic, String text) {
		super(topic);
		
		messages = ArcheMessage.createMultiple(text);
	}

	@Override
	public void output(Player p) {
		for(ChatMessage message : messages)
			message.sendTo(p);
	}
	
	public String outputHelp(){
		StringBuilder builder = new StringBuilder(256);
		String prefix = "";
		for(ChatMessage message : messages){
			builder.append(prefix).append(message.toText());
			prefix = "\n";
		}
		
		return builder.toString();
	}
}
