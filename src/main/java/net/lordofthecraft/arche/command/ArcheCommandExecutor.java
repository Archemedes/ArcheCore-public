package net.lordofthecraft.arche.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.util.AsyncRunner;
import net.lordofthecraft.arche.util.CommandUtil;
import net.lordofthecraft.arche.util.MessageUtil;

@RequiredArgsConstructor
public class ArcheCommandExecutor implements CommandExecutor {
	private final ArcheCommand rootCommand;
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		List<String> listArgs = Arrays.asList(args);
		runCommand(sender, rootCommand, listArgs);
		return true;
	}

	private void runCommand(CommandSender sender, ArcheCommand command, List<String> args) {
		if("help".equals(args.get(0))) {
			help(sender, command);
			return;
		}
		
		ArcheCommand subCommand = wantsSubCommand(command, args.get(0));
		if(subCommand != null) {
			args.remove(0);
			runCommand(sender, subCommand, args);
		} else if (wantsHelpFlag(args)) {
			help(sender, command);
		} else if (!command.hasPermission(sender)) {
			sender.sendMessage(RanCommand.ERROR_PREFIX + "You do not have permission to use this");
		} else {
			if(command.requiresPersona() && sender.hasPermission("archecore.mod.persona")) {
				OfflinePersona pers = personaFlag(args);
				if(pers != null) {
					getPersonaAndExecute(command, sender, args, pers);
					return;
				}
			}

			executeCommand(sender, command, args);
		}
	}
	
	private void executeCommand(CommandSender sender, ArcheCommand command, List<String> args) {
		
		RanCommand c = new RanCommand(command, sender);
		c.parseAll(args);
		
		if(c.isInErrorState()) {
			//TODO
		}
	}
	
	private OfflinePersona personaFlag(List<String> args) {
		return IntStream.range(0, args.size())
		.filter(i-> args.get(i).equals("-p"))
		.map(i->i+1)
		.filter(i -> i < args.size())
		.mapToObj(args::get)
		.findFirst()
		.map(CommandUtil::offlinePersonaFromArg)
		.filter(o->!o.isLoaded())
		.orElse(null);
	}
	
	private void getPersonaAndExecute(ArcheCommand ac, CommandSender sender, List<String> args, OfflinePersona o) {
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Persona not loaded. Will try to load it now. Please wait...");
		AsyncRunner.doAsync(ArcheCore.getPlugin(), ()->o.loadPersona())
		.andThen(persona->{
			ArchePersona otherPersona = ArchePersonaHandler.getInstance()
					.getPersonaStore()
					.addOnlinePersona((ArchePersona)persona);
      if(otherPersona != persona) {
      	CoreLog.warning("Interleaved Persona loading: Persona " + MessageUtil.identifyPersona(persona)
      	+ " has come online while " + sender.getName() +  " also tried to load it.");
      }
      executeCommand(sender, ac, args);
		});
	}
	
	private boolean wantsHelpFlag(List<String> args) {
		return args.stream().anyMatch("-h"::equals);
	}
	
	private ArcheCommand wantsSubCommand(ArcheCommand cmd, String zeroArg) {
		return null;
	}
	
	private void help(CommandSender s, ArcheCommand ac) {
		//todo
	}
	
}
