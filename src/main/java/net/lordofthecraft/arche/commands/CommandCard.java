package net.lordofthecraft.arche.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import co.lotc.core.command.CommandTemplate;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.md_5.bungee.api.chat.BaseComponent;

public class CommandCard extends CommandTemplate {

	public void invoke(CommandSender s, Persona target) {
		List<BaseComponent> whois = ArcheCore.getPersonaControls().whois(target, s);
		whois.forEach(s.spigot()::sendMessage);
	}

}
