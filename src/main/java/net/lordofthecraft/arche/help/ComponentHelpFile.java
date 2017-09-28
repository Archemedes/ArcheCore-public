package net.lordofthecraft.arche.help;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;

/**
 * Help file where we can directly output a BaseComponent
 */
public class ComponentHelpFile extends HelpFile {
	private final BaseComponent help;
	
	public ComponentHelpFile(String topic, BaseComponent help, String permission) {
		super(topic);
		this.help = help;
	}
	
	@Override
	public void output(Player p) {
		p.spigot().sendMessage(help);
	}
	
	@Override
	public String outputHelp(){
		return help.toPlainText();
	}

}
