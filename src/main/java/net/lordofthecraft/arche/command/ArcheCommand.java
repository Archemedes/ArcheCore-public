package net.lordofthecraft.arche.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.PluginCommand;

import lombok.Value;

@Value
public class ArcheCommand {
	String mainCommand;
	List<String> aliases;
	String description;
	String permission;

	boolean requirePlayer = false;
	boolean requirePersona = false;

	List<CmdArg<?>> args = new ArrayList<>();
	List<CmdFlag> flags = new ArrayList<>();
	List<ArcheCommandBuilder> subCommands = new ArrayList<>();

	Map<CommandPart, Boolean> commandStructure = new LinkedHashMap<>();


	public static ArcheCommandBuilder builder(PluginCommand command) {
		return new ArcheCommandBuilder(command);
	}

	public static ArcheCommandBuilder builder(String name) {
		return new ArcheCommandBuilder(name);
	}

}
