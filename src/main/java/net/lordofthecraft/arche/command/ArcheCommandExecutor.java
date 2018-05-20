package net.lordofthecraft.arche.command;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.Lists;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArcheCommandExecutor implements CommandExecutor {
	private final ArcheCommand rootCommand;
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		List<String> cleanedArgs = Stream.of(args)
				.map(String::toLowerCase)
				.collect(Collectors.toCollection(()->Lists.newLinkedList()));
		runCommand(sender, rootCommand, cleanedArgs);
		return true;
	}

	private void runCommand(CommandSender sender, ArcheCommand command, List<String> args) {
		if("help".equals(args.get(0))) {
			//TODO: help output
			return;
		}
		
		ArcheCommand subCommand = wantsSubCommand(command, args.get(0));
		if(subCommand != null) {
			args.remove(0);
			runCommand(sender, subCommand, args);
		} else {
			RanCommand c = new RanCommand(command, args);
		}
		
	}
	
	
	
	private ArcheCommand wantsSubCommand(ArcheCommand cmd, String zeroArg) {
		return null;
	}
	
}
