package net.lordofthecraft.arche.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.bukkit.command.PluginCommand;

import lombok.Setter;
import lombok.experimental.Accessors;

//We're reaching levels of Telanir that shouldn't be even possible
@Accessors(fluent=true)
public class ArcheCommandBuilder {
	private final String mainCommand;
	
	@Setter private String description;
	@Setter private String permission;
	
	boolean requirePlayer = false;
	boolean requirePersona = false;
	
	private final Set<String> aliases = new HashSet<>();
	private final List<CmdArg<?>> args = new ArrayList<>();
	private final List<CmdFlag> flags = new ArrayList<>();
	private final List<ArcheCommandBuilder> subCommands = new ArrayList<>();
	
	private final Map<CommandPart, Boolean> commandStructure = new LinkedHashMap<>();
	
	
	ArcheCommandBuilder(PluginCommand command) {
		//this.pluginCommand = command;
		this.mainCommand = command.getName();
		this.description = command.getDescription();
		this.aliases.addAll(command.getAliases());
		this.permission = command.getPermission();
	}
	
	ArcheCommandBuilder(String name){
		//this.pluginCommand = null;
		this.mainCommand = name;
	}
	
	public ArgBuilder arg() {
		return new ArgBuilder(this);
	}
	
	public ArgBuilder arg(String name) {
		return new ArgBuilder(this).name(name);
	}
	
	void addArg(CmdArg<?> arg) {
		args.add(arg);
	}
	
	void addFlag(CmdFlag flag) {
		flags.add(flag);
	}
	
	public ArcheCommandBuilder alias(String alias) {
		aliases.add(alias);
		return this;
	}
	
	public ArcheCommandBuilder pex(String permission) { //Zero fucks given
		return permission(permission);
	}
	
	public ArcheCommandBuilder requiresPlayer() {
		requirePlayer = true;
		return this;
	}
	
	public ArcheCommandBuilder requiresPersona() {
		requirePersona = true;
		return this;
	}
	
	public ArcheCommandBuilder condition(Predicate<RanCommand> p) {
		
		return this;
	}
	
	public ArcheCommandBuilder run(Consumer<RanCommand> c) {
		commandStructure.put(CommandPart.asConsumer(c), false);
		return this;
	}
	
	public List<ArcheCommandBuilder> getSubCommands(){
		return Collections.unmodifiableList(subCommands);
	}
}
