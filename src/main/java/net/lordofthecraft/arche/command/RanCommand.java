package net.lordofthecraft.arche.command;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Getter;
import net.lordofthecraft.arche.interfaces.Persona;

public class RanCommand {
	@Getter private Persona persona;
	@Getter private Player player;
	
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
		//TODO: a lot
	}
	
	private void parseFlags(ArcheCommand ac, List<String> args) {
		
	}
	
	private void parseArgs(ArcheCommand ac) {
		List<CmdArg<?>> args = ac.getArgs();
		for(int i = 0; i < args.size(); i++) {
			CmdArg<?> arg = args.get(i);
		}
	}
	

}
