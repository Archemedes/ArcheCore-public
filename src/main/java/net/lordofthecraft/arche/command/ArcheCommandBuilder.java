package net.lordofthecraft.arche.command;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lordofthecraft.arche.command.CommandPart.Execution;

//We're reaching levels of Telanir that shouldn't be even possible
@Accessors(fluent=true)
public class ArcheCommandBuilder {
	private final ArcheCommandBuilder parentBuilder;
	private final Plugin plugin;
	@Getter private final String mainCommand;
	@Setter private String description;
	@Setter private String permission;
	
	private boolean requirePlayer = false;
	private boolean requirePersona = false;
	private CmdFlag senderParam = null;
	
	private final Set<String> aliases = new HashSet<>();
	@Getter(AccessLevel.PACKAGE) private final List<CmdArg<?>> args = new ArrayList<>();
	@Getter(AccessLevel.PACKAGE) private final List<CmdFlag> flags = new ArrayList<>();
	private final List<ArcheCommand> subCommands = new ArrayList<>();
	
	private CommandPart firstPart;
	private CommandPart tailPart;
	
	//Builder state booleans
	boolean argsHaveDefaults = false; //When arg is added that has default input
	boolean noMoreArgs = false; //When an unity argument is used
	boolean buildHelpFile = true;
	
	
	ArcheCommandBuilder(PluginCommand command) {
		parentBuilder = null;
		plugin = command.getPlugin();
		
		this.mainCommand = command.getName();
		this.description = command.getDescription();
		this.permission = command.getPermission();
		
		command.getAliases().stream().map(String::toLowerCase).forEach(aliases::add);
		aliases.add(command.getName().toLowerCase());
	}
	
	ArcheCommandBuilder(ArcheCommandBuilder dad, String name){
		parentBuilder = dad;
		plugin = dad.plugin;
		this.mainCommand = name;
		aliases.add(name.toLowerCase());
	}
	
	public ArcheCommandBuilder subCommand(String name) {
		return new ArcheCommandBuilder(this, name);
	}
	
	public ArgBuilder arg(String name) {
		return arg().name(name);
	}
	
	public ArgBuilder arg() {
		if(noMoreArgs) throw new IllegalStateException("This command cannot accept additional arguments.");
		return new ArgBuilder(this);
	}
	
	void addArg(CmdArg<?> arg) {
		if(arg.hasDefaultInput()) argsHaveDefaults = true;
		else if(argsHaveDefaults) throw new IllegalStateException("For command" + this.mainCommand + ": argument at " + (args.size()-1) + " had no default but previous arguments do");
		args.add(arg);
	}
	
	public ArgBuilder param(String name, String... aliases) {
		return CmdFlag.make(this, name, aliases);
	}
	
	public ArgBuilder restrictedParam(String name, String pex, String... aliases) {
		return CmdFlag.make(this, name, pex, aliases);
	}
	
	public ArcheCommandBuilder flag(String name, String... aliases) {
		param(name, aliases).asBoolean(true);
		return this;
	}
	
	public ArcheCommandBuilder restrictedFlag(String name, String pex, String... aliases) {
		restrictedParam(name, pex, aliases).asBoolean(true);
		return this;
	}
	
	void addFlag(CmdFlag flag) {
		flags.add(flag);
	}
	
	public ArcheCommandBuilder alias(String alias) {
		aliases.add(alias.toLowerCase());
		return this;
	}
	
	public ArcheCommandBuilder requiresPlayer() {
		if(senderParam == null) {
			ArgBuilder b = CmdFlag.make(this, "p", "archecore.mod", new String[0]);
			senderParam = b.flag();
			b.asPlayer();
		}
		requirePlayer = true;
		return this;
	}
	
	public ArcheCommandBuilder requiresPersona() {
		if(senderParam == null || (requirePlayer && !requirePersona)) {
			ArgBuilder b = CmdFlag.make(this, "p", "archecore.mod.persona", new String[0]);
			senderParam = b.flag();
			b.asOfflinePersona();
		}
		requirePersona = true;
		return this;
	}
	
	public ArcheCommandBuilder noHelp() {
		buildHelpFile = false;
		return this;
	}
	
	public ArcheCommandBuilder message(String message, Object... format) {
		sequence(CommandPart.messager(message, Execution.SYNC));
		return this;
	}
	
	public ArcheCommandBuilder messageAsync(String message, Object... format) {
		sequence(CommandPart.messager(message, Execution.ANY));
		return this;
	}
	
	public ArcheCommandBuilder condition(Predicate<RanCommand> p, String orElseError) {
		sequence(CommandPart.tester(p, orElseError));
		return this;
	}

	public ArcheCommandBuilder condition(Predicate<RanCommand> p, Consumer<RanCommand> orElse) {
		sequence(CommandPart.tester(p, orElse));
		return this;
	}
	
	public ArcheCommandBuilder run(Consumer<RanCommand> c) {
		sequence(CommandPart.run(c, Execution.SYNC));
		return this;
	}
	
	public ArcheCommandBuilder runAsync(Consumer<RanCommand> c) {
		sequence(CommandPart.run(c, Execution.ASYNC));
		return this;
	}
	
	public ArcheCommandBuilder runConsumer(BiConsumer<RanCommand, Connection> bic) {
		sequence(CommandPart.consume(bic));
		return this;
	}
	
	public <T> CommandPart.JoinedPart<T> fetchConsumer(BiFunction<RanCommand, Connection, T> function){
		return new CommandPart.JoinedPart<>(this, function);
	}
	
	public <T> CommandPart.JoinedPart<T> fetchAsync(Function<RanCommand, T> function){
		return CommandPart.JoinedPart.forAsync(this, function);
	}
	
	void sequence(CommandPart newPart) {
		if(tailPart == null) firstPart = newPart;
		else tailPart.setNext(newPart);
		
		tailPart = newPart;
		tailPart.setPlugin(plugin);
	}
	
	public ArcheCommandBuilder build() {
		ArcheCommand built = new ArcheCommand(
				mainCommand,
				Collections.unmodifiableSet(aliases),
				description,
				permission,
				requirePlayer,
				requirePersona,
				Collections.unmodifiableList(args),
				Collections.unmodifiableList(flags),
				Collections.unmodifiableList(subCommands),
				firstPart);
		
		if(buildHelpFile) {
			this.subCommands.add(new HelpCommand(built));
			this.flag("h", "help");
		}
		
		if(parentBuilder != null) {
			parentBuilder.subCommands.add(built);
			//TODO collision checks
		}
		return parentBuilder;
	}
	
}
