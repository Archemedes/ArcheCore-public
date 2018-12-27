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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.command.CommandPart.Execution;
import net.lordofthecraft.arche.command.brigadier.Kommandant;

//We're reaching levels of Telanir that shouldn't be even possible
@Accessors(fluent=true)
public class ArcheCommandBuilder {
	private final ArcheCommandBuilder parentBuilder;
	private final PluginCommand command;
	
	@Getter private final String mainCommand;
	@Setter private String description;
	@Setter private String permission;
	
	private boolean requirePlayer = false;
	private boolean requirePersona = false;
	private CmdFlag senderParam = null;
	
	@Getter(AccessLevel.PACKAGE) private final Set<String> aliases = new HashSet<>();
	@Getter(AccessLevel.PACKAGE) private final List<CmdArg<?>> args = new ArrayList<>();
	@Getter(AccessLevel.PACKAGE) private final List<CmdFlag> flags = new ArrayList<>();
	@Getter(AccessLevel.PACKAGE) private final List<ArcheCommand> subCommands = new ArrayList<>();
	
	private CommandPart firstPart;
	private CommandPart tailPart;
	
	//Builder state booleans
	boolean brigadier = true; //Can turn brigadier control off, defaults to spigot completions
	boolean argsHaveDefaults = false; //When arg is added that has default input
	boolean noMoreArgs = false; //When an unity argument is used
	boolean buildHelpFile = true;
	
	
	public ArcheCommandBuilder(PluginCommand command) {
		parentBuilder = null;
		this.command = command;
		
		this.mainCommand = command.getName();
		this.description = command.getDescription();
		this.permission = command.getPermission();
		
		command.getAliases().stream().map(String::toLowerCase).forEach(aliases::add);
		aliases.add(command.getName().toLowerCase());
	}
	
	ArcheCommandBuilder(ArcheCommandBuilder dad, String name, boolean inheritOptions){
		parentBuilder = dad;
		command = dad.command;
		this.mainCommand = name;
		this.permission = dad.permission;
		aliases.add(name.toLowerCase());
		if(inheritOptions) {
			this.buildHelpFile = dad.buildHelpFile;
			if(dad.requirePersona) requiresPersona();
			if(dad.requirePlayer) requiresPlayer();
		}
	}
	
	public ArcheCommandBuilder subCommand(String name) {
		return subCommand(name, true);
	}
	
	public ArcheCommandBuilder subCommand(String name, boolean inheritOptions) {
		return new ArcheCommandBuilder(this, name, inheritOptions);
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
	
	public ArgBuilder flag(String name, String... aliases) {
		return CmdFlag.make(this, name, aliases);
	}
	
	public ArgBuilder restrictedFlag(String name, String pex, String... aliases) {
		return CmdFlag.make(this, name, pex, aliases);
	}
	
	void addFlag(CmdFlag flag) {
		flags.add(flag);
	}
	
	public ArcheCommandBuilder alias(String... aliases) {
		for(String alias : aliases) this.aliases.add(alias.toLowerCase());
		return this;
	}
	
	public ArcheCommandBuilder requiresPlayer() {
		CoreLog.debug("cmd " + mainCommand() + " requires Player");
		if(senderParam == null) {
			ArgBuilder b = CmdFlag.make(this, "p", "archecore.mod", new String[0]);
			senderParam = b.flag();
			b.asPlayer();
		}
		requirePlayer = true;
		return this;
	}
	
	public ArcheCommandBuilder requiresPersona() {
		CoreLog.debug("cmd " + mainCommand() + " requires Persona");
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
	
	public ArcheCommandBuilder noBrigadier() {
		brigadier = false;
		if(parentBuilder != null) parentBuilder.noBrigadier();
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
		tailPart.setPlugin(command.getPlugin());
	}
	
	public ArcheCommandBuilder build() {
		boolean noneSpecified = firstPart == null;
		if(noneSpecified) {
			if(!args.isEmpty() || subCommands.isEmpty())
				throw new IllegalStateException("Found no execution sequence for command: " + this.mainCommand
						+ ". This is only possible if the command has subcommands and no arguments specified."
						+ " It is VERY likely the command was built incorrectly.");
			firstPart = CommandPart.run($->{}, Execution.SYNC);
		}
		
		CoreLog.debug("Now Building ArcheCommand: " + mainCommand + " it has " + subCommands.size()
			+ " subcommands and parent: " +(parentBuilder == null? "none":parentBuilder.mainCommand));
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
		
		if(parentBuilder != null) {
			if(built.collides(parentBuilder.subCommands))
				throw new IllegalStateException("Detected ambiguous subcommand: "
			  + built.getMainCommand() + ". Aliases and argument range overlap with other commands!");
			parentBuilder.subCommands.add(built);
		}
		
		if(buildHelpFile) {
			HelpCommand help = new HelpCommand(built);
			this.subCommands.add(help);
			if(noneSpecified) firstPart.setNext(CommandPart.run(c->help.runHelp(c, 0), Execution.SYNC));
			flag("h").description("Get help and subcommands").defaultInput("0").asInt();
		}
		
		//If there's no more builders up the chain we've reached the top. Means we're done and we can make an executor
		if(parentBuilder == null) {
			ArcheCommandExecutor executor = new ArcheCommandExecutor(built);
			command.setExecutor(executor);
		}
		
		if(brigadier && parentBuilder == null) {
			new Kommandant(command, built).addBrigadier();
		}
		
		return parentBuilder;
	}
}