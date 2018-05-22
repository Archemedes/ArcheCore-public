package net.lordofthecraft.arche.command;

import static org.bukkit.ChatColor.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class RanCommand {
	public  static final String ERROR_PREFIX = DARK_RED + "Error: " + RED;
	private static final String ERROR_FLAG_ARG = " Not a valid flag argument provided for: " + WHITE;
	private static final String ERROR_NEEDS_PLAYER = " This command can only be run by players.";
	private static final String ERROR_NEEDS_PERSONA = " You need a valid Persona to run this command";
	
	final ArcheCommand command;
	
	@Getter final CommandSender sender;
	@Getter Player player;
	@Getter Persona persona;
	
	
	List<Object> argResults = Lists.newArrayList();
	Map<String, Object> context = Maps.newHashMap();
	Map<String, Object> flags = Maps.newHashMap();
	
	@Getter(AccessLevel.PACKAGE) boolean inErrorState = false;
	@Getter(AccessLevel.PACKAGE) String errorMessage = null;
	
	
	@SuppressWarnings("unchecked")
	public <T> T getArg(int i) { //Static typing is for PUSSIES
		return (T) argResults.get(i);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getFlag(String flagName) {
		return (T) flags.get(flagName);
	}
	
	public boolean hasFlag(String flagName) {
		return flags.containsKey(flagName);
	}
	
	public void addContext(String key, Object value) {
		context.put(key, value);
	}
	
	RanCommand(ArcheCommand producer, CommandSender s){
		command = producer;
		sender = s;
	}
	
	void parseAll(List<String> args) {
		try {
			parseFlags(args);
			getSenders();
			parseArgs(args);
		} catch(CmdParserException e) {
			//We basically use this to signal a user error somewhere. Err msg will be send to CommandSender
			inErrorState = true;
			errorMessage = e.getMessage();
		}
	}
	
	private void getSenders() throws CmdParserException {
		if(command.requiresPersona()) {
			OfflinePersona potentialPersona = getFlag("p");
			if(potentialPersona != null && potentialPersona.isLoaded()) {
				persona = potentialPersona.getPersona();
			} else if(potentialPersona == null) {
				if(sender instanceof Player) persona = ArcheCore.getPersona((Player) sender);
				
				if(persona == null) error(ERROR_NEEDS_PERSONA);
				else player = persona.getPlayer();
				
				if(player == null && command.requiresPlayer()) error(ERROR_NEEDS_PLAYER);
			}
			
			//The third case is when a potentialPersona exists but is unloaded.
			//the ArcheCommandExecutor will pick up on this lacking persona and try to load
			//So we do nothing additionally here.
			
		} else if (command.requiresPlayer()) {
			player = (Player) flags.get("p");
			if(player == null && sender instanceof Player) player = (Player) sender;
			if(player == null) error(ERROR_NEEDS_PLAYER);
		}
	}
	
	void rectifySenders(Persona persona) {
		this.persona = persona;
		this.flags.put("p", persona);
	}
	
	private void parseFlags(List<String> args) throws CmdParserException {
	//Father forgive me for I have sinned
		List<CmdFlag> f = new ArrayList<>(command.getFlags());
		for(int i = 0; i < args.size(); i++) {
			String a = args.get(i);
			if(a.startsWith("-")) {
				CmdFlag flag = matchFlag(a, f);
				if(flag != null) {
					if(!flag.mayUse(sender))
					f.remove(flag);
					args.remove(i);
					String flagArg;
					if(i < args.size() && !args.get(i).startsWith("-")) {
						flagArg = args.remove(i);
						i -= 1; //NB
					} else {
						flagArg = flag.getArg().getDefaultInput();
					}
					
					Object resolved = Optional.of(flagArg).map(flag.getArg()::resolve).orElse(null);
					if(resolved == null) error(ERROR_FLAG_ARG + flag.getName());
					else flags.put(flag.getName(), resolved);
				}
			}
		}
	}
	
	private CmdFlag matchFlag(String input, List<CmdFlag> flags) {
		String xput = input.substring(1).toLowerCase();
		for(CmdFlag flag : flags) {
			boolean flagFound = flag.getAliases().stream().filter(f->f.equals(xput)).findAny().isPresent();
			if(flagFound) return flag;
		}
		
		return null;
	}
	
	private void parseArgs(List<String> args) throws CmdParserException {
		List<CmdArg<?>> cmdArgs = command.getArgs();
		for(int i = 0; i < cmdArgs.size(); i++) {
			CmdArg<?> arg = cmdArgs.get(i);
			Object o = null;
			if(i >= args.size()) o = arg.resolveDefault();
			else o = arg.resolve(args.get(i));
			
			if(o == null) error("at argument" + i + ": " + arg.getErrorMessage());
			else argResults.add(o);
		}
	}
	
	private void error(String err) throws CmdParserException {
		throw new CmdParserException(err);
	}
	
	private static class CmdParserException extends Exception{
		private static final long serialVersionUID = 5283812808389224035L;

		private CmdParserException(String err) {
			super(err);
		}
	}
	
}
