package net.lordofthecraft.arche.command;

import java.util.Arrays;
import java.util.List;

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
import net.lordofthecraft.arche.util.MessageUtil;

@RequiredArgsConstructor
public class ArcheCommandExecutor implements CommandExecutor {
	private final ArcheCommand rootCommand;
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		List<String> listArgs = Arrays.asList(args);
		runCommand(sender, rootCommand, label, listArgs);
		return true;
	}

	private void runCommand(CommandSender sender, ArcheCommand command, String usedAlias, List<String> args) {
		ArcheCommand subCommand = wantsSubCommand(command, args);
		if(subCommand != null) {
			runSubCommand(sender, subCommand, usedAlias, args);
		} else if (!command.hasPermission(sender)) {
			sender.sendMessage(RanCommand.ERROR_PREFIX + "You do not have permission to use this");
		} else {
			RanCommand c = new RanCommand(command, sender);
			c.parseAll(args);
			if(c.isInErrorState()) {
				sender.sendMessage(RanCommand.ERROR_PREFIX + c.getErrorMessage());
			} else if(command.hasHelp() && c.hasFlag("h") && (boolean) c.getFlag("h")) {
				runSubCommand(sender, command.getHelp(), "h", args);
			} else if(command.requiresPersona() && c.getPersona() == null){
				//The case where a persona flag was offered, meaning the command is not YET in error.
				OfflinePersona pers = c.getFlag("p");
				getPersonaAndExecute(sender, command, c, args, pers);
			} else {
				executeCommand(sender, command, c, args);
			}
		}
	}
	
	private void runSubCommand(CommandSender sender, ArcheCommand subCommand, String usedAlias, List<String> args) {
		String usedSubcommandAlias = args.remove(0).toLowerCase();
		String newAlias = usedAlias + ' ' + usedSubcommandAlias;
		runCommand(sender, subCommand, newAlias, args);
	}
	
	private void executeCommand(CommandSender sender, ArcheCommand command, RanCommand c,  List<String> args) {
		//TODO
	}
	
	private void getPersonaAndExecute( CommandSender sender, ArcheCommand ac, RanCommand c, List<String> args, OfflinePersona o) {
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Persona not loaded. Will try to load it now. Please wait...");
		AsyncRunner.doAsync(ArcheCore.getPlugin(), ()->o.loadPersona())
		.andThen(persona->{
			ArchePersona otherPersona = ArchePersonaHandler.getInstance().getPersonaStore()
					.addOnlinePersona((ArchePersona)persona);
      if(otherPersona != persona) {
      	CoreLog.warning("Interleaved Persona loading: Persona " + MessageUtil.identifyPersona(persona)
      	+ " has come online while " + sender.getName() +  " also tried to load it.");
      }
      c.rectifySenders(otherPersona);
      executeCommand(sender, ac, c, args);
		});
	}
	
	private ArcheCommand wantsSubCommand(ArcheCommand cmd, List<String> args) {
		if(args.size() == 0) return null;
		
		String possibleSubcommand = args.get(0).toLowerCase();
		for(ArcheCommand sub : cmd.getSubCommands()) {
			if(sub.getAliases().stream().anyMatch(possibleSubcommand::equals)) return sub;
		}
		
		return null;
	}
	
}
