package net.lordofthecraft.arche.commands;

import org.bukkit.command.CommandSender;

import co.lotc.core.command.CommandTemplate;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;

public class CommandCard extends CommandTemplate {

	public void invoke(CommandSender s, Persona target) {
		ArcheCore.getPersonaControls().whois(target, s);
	}

}
