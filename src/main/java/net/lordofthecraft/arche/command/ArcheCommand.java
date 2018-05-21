package net.lordofthecraft.arche.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class ArcheCommand {
	String mainCommand;
	List<String> aliases;
	String description;
	String permission;

	@Getter(AccessLevel.NONE) boolean requirePlayer = false;
	@Getter(AccessLevel.NONE) boolean requirePersona = false;

	List<CmdArg<?>> args = new ArrayList<>();
	List<CmdFlag> flags = new ArrayList<>();
	List<ArcheCommandBuilder> subCommands = new ArrayList<>();

	Map<CommandPart, Boolean> commandStructure = new LinkedHashMap<>();


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
}
