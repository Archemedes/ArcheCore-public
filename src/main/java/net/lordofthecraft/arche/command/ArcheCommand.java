package net.lordofthecraft.arche.command;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;

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

	@Getter(AccessLevel.NONE) Map<CommandPart, Boolean> commandStructure = new LinkedHashMap<>();


	public static ArcheCommandBuilder builder(PluginCommand command) {
		return new ArcheCommandBuilder(command);
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
	
	public boolean hasHelp() {
		return subCommands.stream().anyMatch(HelpCommand.class::isInstance);
	}
	
	HelpCommand getHelp() {
		return subCommands.stream()
				.filter(HelpCommand.class::isInstance)
				.map(HelpCommand.class::cast)
				.findAny().orElse(null);
	}
	
	void processStructure(ArcheCommandBuilder builder) {
		
	}
}
