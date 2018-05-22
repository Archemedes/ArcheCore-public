package net.lordofthecraft.arche.command;

import java.util.Arrays;
import java.util.Collections;

import com.google.common.primitives.Ints;

import lombok.val;

public class HelpCommand extends ArcheCommand {
	private final ArcheCommand parent;
	
	HelpCommand(ArcheCommand ac) {
	  super("help", 
	  		Collections.emptySet(), 
	  		"prints help", 
	  		ac.getPermission(), 
	  		false, 
	  		false, 
	  		Arrays.asList(helpPageArg()), 
	  		Collections.emptyList(),
	  		Collections.emptyList());
	  
	  parent = ac;
	  
	}
	
	private static CmdArg<Integer> helpPageArg(){
		val c = new CmdArg<Integer>("page", "not a valid integer", "0");
		c.setFilter(i->i>=0);
		c.setMapper(Ints::tryParse);
		return c;
	}
	
	private void outputBaseHelp(RanCommand c) {
		
	}
	
	private void outputSubcommands(RanCommand c, int page) {
		//Arrays indexes start at 1
	}
	

}
