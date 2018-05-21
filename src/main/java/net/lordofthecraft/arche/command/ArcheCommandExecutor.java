package net.lordofthecraft.arche.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.interfaces.Persona;

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
		} else {
			RanCommand c = new RanCommand(command, sender);
			c.parseAll(args);
			
			if(c.isInErrorState()) {
				//TODO
			}
		}
		
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
