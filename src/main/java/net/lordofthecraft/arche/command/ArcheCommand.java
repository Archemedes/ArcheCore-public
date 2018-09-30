package net.lordofthecraft.arche.command;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.val;
import lombok.experimental.NonFinal;

//TODO XXX
// completions
// annotation stuff
// joint interface MEHHHH
// other stuff idk
// subcommand overloading DONE
// make sure the errors are properly caught MAYBE DONE

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


	/**
	 * TODO
	 * @param command The PluginCommand to wrap, defined by your plugin through YML or annotation
	 * @return a chainable builder that will construct a CommandExecutor
	 */
	public static ArcheCommandBuilder builder(PluginCommand command) {
		return new ArcheCommandBuilder(command);
	}
	
	/**
	 * @param command The PluginCommand to wrap, defined by your plugin through YML or annotation
	 * @param template Object that creates instances of your CommandTemplate implementation. This should return distinct instances if you plan on using BukkitRunnables at all
	 */
	public static void buildfromTemplate(PluginCommand command, Supplier<CommandTemplate> template) {
		new AnnotatedCommandParser(template, command).invokeParse().build();
	}
	
	public static ArcheCommandBuilder getfromTemplate(PluginCommand command, Supplier<CommandTemplate> template) {
		return new AnnotatedCommandParser(template, command).invokeParse();
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
	
	public boolean isAlias(String param) {
		return aliases.contains(param);
	}
	
	private boolean aliasOverlaps(ArcheCommand other) {
		return aliases.stream().anyMatch(other::isAlias);
	}
	
	private boolean argRangeOverlaps(ArcheCommand other) { //b1 <= a2 && a1 <= b2
		return other.minArgs() <= maxArgs() && minArgs() <= other.maxArgs();
	}
	
	boolean collides(List<ArcheCommand> subbos) {
		return subbos.stream()
		.filter(this::argRangeOverlaps)
		.anyMatch(this::aliasOverlaps);
	}
	
	HelpCommand getHelp() {
		return subCommands.stream()
				.filter(HelpCommand.class::isInstance)
				.map(HelpCommand.class::cast)
				.findAny().orElse(null);
	}
	
	private int minArgs() {
		int i = 0;
		for(val arg : args) {
			if(arg.hasDefaultInput()) return i;
			i++;
		}
		
		return i;
	}
	
	private int maxArgs() {
		int s = args.size();
		if(s > 0 && args.get(s-1) instanceof JoinedArg) return 255;
		else return s;
	}
	
	public boolean fitsArgSize(int argSize) {
		return argSize >= minArgs() && argSize <= maxArgs();
	}
	
	
	
}
