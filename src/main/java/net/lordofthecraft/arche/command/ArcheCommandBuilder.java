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
	private final ArcheCommandBuilder parentBuilder;
	private final String mainCommand;
	
	@Setter private String description;
	@Setter private String permission;
	
	private boolean requirePlayer = false;
	private boolean requirePersona = false;
	private CmdFlag senderFlag;
	
	private final Set<String> aliases = new HashSet<>();
	private final List<CmdArg<?>> args = new ArrayList<>();
	private final List<CmdFlag> flags = new ArrayList<>();
	private final List<ArcheCommand> subCommands = new ArrayList<>();
	
	private final Map<CommandPart, Boolean> commandStructure = new LinkedHashMap<>();
	
	//Builder state booleans
	boolean argsHaveDefaults = false;
	boolean noMoreArgs = false; //When an unity argument is used
	
	
	ArcheCommandBuilder(PluginCommand command) {
		parentBuilder = null;
		
		this.mainCommand = command.getName();
		this.description = command.getDescription();
		this.permission = command.getPermission();
		
		command.getAliases().stream().map(String::toLowerCase).forEach(aliases::add);
	}
	
	ArcheCommandBuilder(ArcheCommandBuilder dad, String name){
		parentBuilder = dad;
		this.mainCommand = name;
	}
	
	public ArgBuilder arg() {
		if(noMoreArgs) throw new IllegalStateException("This command cannot accept additional arguments.");
		return new ArgBuilder(this);
	}
	
	public ArgBuilder arg(String name) {
		return arg().name(name);
	}
	
	void addArg(CmdArg<?> arg) {
		args.add(arg);
	}
	
	public ArgBuilder flag(String name, String... aliases) {
		return CmdFlag.make(this, name, aliases);
	}
	
	public ArgBuilder restrictedFlag(String name, String pex, String... aliases) {
		return CmdFlag.make(this, name, pex, aliases);
	}
	
	public ArcheCommandBuilder arglessFlag(String name, String... aliases) {
		flag(name, aliases).asBoolean(true);
		return this;
	}
	
	public ArcheCommandBuilder restrictedArglessFlag(String name, String pex, String... aliases) {
		restrictedFlag(name, pex, aliases).asBoolean(true);
		return this;
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
	
	public ArcheCommandBuilder build() {
		return parentBuilder;
	}
	
}
