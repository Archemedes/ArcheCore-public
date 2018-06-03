package net.lordofthecraft.arche.command;

import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.lang.ObjectUtils;
import org.bukkit.command.CommandSender;

import com.google.common.primitives.Ints;

import lombok.val;
import net.md_5.bungee.api.ChatColor;

import static org.bukkit.ChatColor.*;

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
		CommandSender s = c.getSender();
		Object desc = ObjectUtils.defaultIfNull(parent.getDescription(), "--None given--");
		s.sendMessage("Description: " + GRAY + "" + ITALIC + desc);
		
		String perm = parent.getPermission();
		if(perm != null) s.sendMessage(ChatColor.GREEN + "Permission: " + ChatColor.YELLOW + perm);
		
		List<String> describedA
	}
	
	private void outputSubcommands(RanCommand c, int page) {
		//Arrays indexes start at 1
	}
	

}
