package net.lordofthecraft.arche.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;

@RequiredArgsConstructor
public class CommandNewbies implements CommandExecutor{
	private final PersonaHandler handler;
	
	public CommandNewbies(PersonaHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String command, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		Player p = (Player) sender;
		List<Persona> pp = Lists.newArrayList();
		Persona holder;
		int radius = 0;
		if (args.length == 0) {
			radius = 50;
		} else {
			try {
				radius = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				radius = 50;
				sender.sendMessage("We couldn't get a number from "+args[0]+" so we're defaulting to 50");
			}
		}
		for (Player pl : p.getPlayer().getWorld().getPlayers()) {
			if (handler.hasPersona(pl) 
					&& (p.getLocation().distance(pl.getLocation()) < radius)) {
				holder = handler.getPersona(pl);
				if (holder.isNewbie()) {
					pp.add(holder);
				}
			}
		}
		sender.sendMessage(ChatColor.AQUA+".:: New player personas in a "+radius+" radius ::.");
		int count = 1;
		for (Persona pe : pp) {
			sender.sendMessage(count+". "+pe.getPlayerName());
			++count;
		}
		return true;
	}
}
