package net.lordofthecraft.arche.command;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.util.AsyncRunner;
import net.lordofthecraft.arche.util.MessageUtil;

@RequiredArgsConstructor
public class ArcheCommandExecutor implements TabExecutor {
	private final ArcheCommand rootCommand;
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		List<String> listArgs = Arrays.asList(args);
		runCommand(sender, rootCommand, label, listArgs);
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

	private void runCommand(CommandSender sender, ArcheCommand command, String usedAlias, List<String> args) {
		ArcheCommand subCommand = wantsSubCommand(command, args);
		if(subCommand != null) {
			runSubCommand(sender, subCommand, usedAlias, args);
		} else if (!command.hasPermission(sender)) {
			sender.sendMessage(RanCommand.ERROR_PREFIX + "You do not have permission to use this");
		} else {
			RanCommand c = new RanCommand(command, usedAlias, sender);
			c.parseAll(args);
			if(c.isInErrorState()) {
				if( !StringUtils.isEmpty(c.getErrorMessage())) sender.sendMessage(RanCommand.ERROR_PREFIX + c.getErrorMessage());
			} else if(command.hasHelp() && c.hasFlag("h") && (boolean) c.getFlag("h")) {
				runSubCommand(sender, command.getHelp(), "h", args);
			} else if(command.requiresPersona() && c.getPersona() == null){
				//The case where a persona flag was offered, meaning the command is not YET in error.
				OfflinePersona pers = c.getFlag("p");
				getPersonaAndExecute(sender, command, c, args, pers);
			} else {
				executeCommand(command, c);
			}
		}
	}
	
	private void runSubCommand(CommandSender sender, ArcheCommand subCommand, String usedAlias, List<String> args) {
		String usedSubcommandAlias = args.remove(0).toLowerCase();
		String newAlias = usedAlias + ' ' + usedSubcommandAlias;
		runCommand(sender, subCommand, newAlias, args);
	}
	
	private void executeCommand(ArcheCommand command, RanCommand c) {
		command.execute(c);
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
      executeCommand(ac, c);
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
