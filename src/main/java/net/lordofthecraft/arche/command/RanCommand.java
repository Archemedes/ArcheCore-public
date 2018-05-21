package net.lordofthecraft.arche.command;

import static org.bukkit.ChatColor.WHITE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Getter;
import net.lordofthecraft.arche.interfaces.Persona;

public class RanCommand {
	private static final String INVALID_FLAG_ARG = " Not a valid flag argument provided for: " + WHITE;
	
	@Getter private CommandSender sender;
	@Getter private Player player;
	@Getter private Persona persona;
	
	
	private List<Object> argResults = Lists.newArrayList();
	private Map<String, Object> context = Maps.newHashMap();
	private Map<CmdFlag, Object> flags = Maps.newHashMap();
	
	private boolean errorState = false;
	private String errorMessage = null;
	
	public Object getArg(int i) {
		return argResults.get(i);
	}
	
	public void addContext(String key, Object value) {
		context.put(key, value);
	}
	
	RanCommand(ArcheCommand producer, List<String> args){
		parseFlags(producer, args);
		if(errorState) return;
		parseArgs(producer, args);
		if(errorState) return;
	}
	
	private void parseFlags(ArcheCommand ac, List<String> args) {
		List<CmdFlag> f = new ArrayList<>(ac.getFlags());
		for(int i = 0; i < args.size(); i++) {
			String a = args.get(i);
			if(a.startsWith("-")) {
				CmdFlag flag = matchFlag(a, f);
				if(flag != null) {
					f.remove(flag);
					args.remove(i);
					String flagArg;
					if(i < args.size() && !args.get(i).startsWith("-")) {
						flagArg = args.remove(i);
						i -= 1;
					} else {
						flagArg = flag.getArg().getDefaultInput();
					}
					
					if(flagArg == null) {
						errorState = true;
						errorMessage = INVALID_FLAG_ARG + flag.getName();
					} else {
						Object resolve = flag.getArg().resolve(flagArg);
						
					}
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
	
	private void parseArgs(ArcheCommand ac, List<String> args) {
		List<CmdArg<?>> cmdArgs = ac.getArgs();
		for(int i = 0; i < cmdArgs.size(); i++) {
			CmdArg<?> arg = cmdArgs.get(i);
		}
	}
	

}
