package net.lordofthecraft.arche.command;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;

//TODO XXX
// completions
// annotation stuff
// joint interface
// other stuff idk
// subcommand overloading
// make sure the errors are properly caught

@Value
@NonFinal
public class ArcheCommand {
	String mainCommand;
	Set<String> aliases;
	String description;
	String permission;

	@Getter(AccessLevel.NONE) boolean requirePlayer;
	@Getter(AccessLevel.NONE) boolean requirePersona;

	List<CmdArg<?>> args;
	List<CmdFlag> flags;
	List<ArcheCommand> subCommands;

	@Getter(AccessLevel.NONE) CommandPart sequenceStart;


	public static ArcheCommandBuilder builder(PluginCommand command) {
		return new ArcheCommandBuilder(command);
	}
	
	void execute(RanCommand rc) {
		sequenceStart.execute(rc);
	}

	public boolean requiresPlayer() {
		return requirePlayer;
	}
	
	public boolean requiresPersona() {
		return requirePersona;
	}
	
	public boolean hasPermission(CommandSender s) {
		return StringUtils.isEmpty(permission) || s.hasPermission(permission);
	}
	
	public boolean hasDescription() {
		return StringUtils.isNotEmpty(description);
	}
	
	public boolean hasHelp() {
		return subCommands.stream().anyMatch(HelpCommand.class::isInstance);
	}
	
	HelpCommand getHelp() {
		return subCommands.stream()
				.filter(HelpCommand.class::isInstance)
				.map(HelpCommand.class::cast)
				.findAny().orElse(null);
	}
	
}
