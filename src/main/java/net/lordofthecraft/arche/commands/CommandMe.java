package net.lordofthecraft.arche.commands;

import org.bukkit.entity.Player;

import co.lotc.core.command.CommandTemplate;
import net.lordofthecraft.arche.ArcheBeacon;

public class CommandMe extends CommandTemplate {

	public void invoke(Player p) {
		ArcheBeacon.openBeacon(p);
	}

}
