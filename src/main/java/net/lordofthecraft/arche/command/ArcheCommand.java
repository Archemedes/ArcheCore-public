package net.lordofthecraft.arche.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.PluginCommand;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

//We're reaching levels of Telanir that shouldn't be even possible
@Accessors(fluent=true)
public class ArcheCommand {
	private String mainCommand;
	private List<String> aliases;
	@Setter private String description;
	@Setter private String permission;
	
	boolean requirePlayer = false;
	boolean requirePersona = false;
	
	private final PluginCommand pluginCommand;
	
	private final List<CmdArg<?>> args = new ArrayList<>();
	private final List<CmdFlag> flags = new ArrayList<>();
	@Getter private final List<ArcheCommand> subCommands = new ArrayList<>();
	
	
	public ArcheCommand(PluginCommand command) {
		this.pluginCommand = command;
		this.description = command.getDescription();
		this.aliases = command.getAliases();
	}
	
	public ArgBuilder arg() {
		return arg(null);
	}
	
	public ArgBuilder arg(String defaultValue) {
		ArgBuilder builder = new ArgBuilder(this, defaultValue);
		
		
		return builder;
	}
	
	void addArg(CmdArg<?> arg) {
		args.add(arg);
	}
	
	public ArcheCommand pex(String permission) { //Zero fucks given
		return permission(permission);
	}
	
	public ArcheCommand requiresPlayer() {
		requirePlayer = true;
		return this;
	}
	
	public ArcheCommand requiresPersona() {
		requirePersona = true;
		return this;
	}
	
	//condition
	
	//run
	
	//runAsync
	
	//runConsumer
	
	//msg
	
}
