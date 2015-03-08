package net.lordofthecraft.arche.help;

import org.bukkit.entity.Player;

/**
 * Represents a plain text help file.
 */
public class PlainHelpFile extends HelpFile {
	private final String output;
	
	public PlainHelpFile(String topic, String output) {
		super(topic);
		this.output = output;
	}

	@Override
	public void output(Player p) {
		p.sendMessage(output);
	}

	@Override
	public String outputHelp() {
		return output;
	}

}
