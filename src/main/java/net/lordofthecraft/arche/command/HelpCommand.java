package net.lordofthecraft.arche.command;

import static org.bukkit.ChatColor.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.ObjectUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.primitives.Ints;

import lombok.val;
import lombok.experimental.ExtensionMethod;
import net.lordofthecraft.arche.util.ChatBuilder;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.extension.ArcheExtension;
import net.lordofthecraft.arche.util.extension.UtilExtension;

@ExtensionMethod({ArcheExtension.class,UtilExtension.class})
public class HelpCommand extends ArcheCommand {
	private static final ChatColor[] colors = new ChatColor[] {BLUE, LIGHT_PURPLE, AQUA, GREEN, YELLOW, DARK_GRAY, GOLD, RED, DARK_AQUA};
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
		val c = new CmdArg<Integer>("page", "not a valid integer", "0", null);
		c.setFilter(i->i>=0);
		c.setMapper(Ints::tryParse);
		return c;
	}
	
	private void outputBaseHelp(RanCommand c) {
		CommandSender s = c.getSender();
		commandHeadline(c).send(s);
		
		Object desc = ObjectUtils.defaultIfNull(parent.getDescription(), "--None given--");
		s.sendMessage("Description: " + GRAY + "" + ITALIC + desc);
		
		String perm = parent.getPermission();
		if(perm != null) s.sendMessage(GREEN + "Permission: " + YELLOW + perm);
		
		val argHelp = getArgumentHelp(s);
		if(!argHelp.isEmpty()) s.sendMessage(ChatColor.DARK_GRAY + "-=== Takes the following parameters: ===-");
		argHelp.forEach(s::sendMessage);
	}
	
	private ChatBuilder commandHeadline(RanCommand c) {
		String alias = "/" + c.getUsedAlias();
		if(alias.endsWith("help")) alias = alias.substring(0, alias.length() - 5);
		ChatBuilder b = MessageUtil.builder(alias);
		b.color(ChatColor.GOLD).suggest(alias);
		fillArgs(b, true);
		return b;
	}
	
	private void fillArgs(ChatBuilder b, boolean useColor) {
		int i = 0;
		for(CmdArg<?> a : parent.getArgs()) {
			boolean optional = a.hasDefaultInput();
			b.append(" ");
			if(useColor) b.color(colorCoded(i++));
			b.append(optional? "{":"[")
			 .append(a.getName())
			 .append(optional? "}":"]");
		}
	}
	
	private List<String> getArgumentHelp(CommandSender s) {
		val result = new ArrayList<String>();
		
		int i = 0;
		for(CmdArg<?> a : parent.getArgs()) {
			if(a.hasDescription()) result.add( colorCoded(i) + a.getName() + ": " + GRAY + a.getDescription());
			i++;
		}
		
		for(CmdFlag flag : parent.getFlags()) {
			if(!flag.needsPermission() || s.hasPermission(flag.getPermission())){
				StringBuilder b = new StringBuilder(flag.getName());
				if(flag.getArg().hasDescription())
					b.append(": ").append(GRAY).append(flag.getArg().getDescription());
				if(flag.needsPermission())
					b.append(GREEN).append(" (perm: ").append(flag.getPermission()).append(")");
				
				result.add(b.toString());
			}
		}
		
		return result;
	}
	
	private ChatColor colorCoded(int i) {
		return colors[i%colors.length];
	}
	
	private void outputSubcommands(CommandSender s, RanCommand c, int page) {
		List<ArcheCommand> subs = parent.getSubCommands().stream()
				.filter(sub->sub!=this)
				.filter(sub->sub.hasPermission(s))
				.collect(Collectors.toList());
		
		String alias = "/" + c.getUsedAlias();
		if(alias.endsWith("help")) alias = alias.substring(0, alias.length() - 5);
		
		
		if(subs.size() < page*6) {
			s.sendMessage(RanCommand.ERROR_PREFIX + "Invalid help page!");
			return; //haha fuck you readability
		} else {
			s.sendMessage(GOLD + "-== Possible sub-commands for " + GRAY + alias + GOLD + " ==-");
		}
		
		//Arrays start at 1 fight me
		for(int i = page*6; i < page*6+6; i++) {
			if(subs.size() <= i) break;
			ArcheCommand sub = subs.get(i);
			String subber = sub.getMainCommand();
			ChatBuilder b = MessageUtil.builder(subber)
					.color(GOLD)
					.command(alias + ' ' + subber + " help")
					.hover(GRAY + "Click for help on this subcommand!");
			fillArgs(b, false);
			
			if(sub.hasDescription()) {
				b.append(": ");
				int room = 60 - b.toPlainText().length();
				String desc = sub.getDescription();
				if(desc.length() > room) desc = desc.substring(0, room) + '\u2026';
				b.append(desc);
			}
			
			b.send(s);
		}
		
		ChatBuilder b = MessageUtil.builder();
		if(page > 1) b.appendButton("Prev Page", alias+" -h" + (page-1)).append(" ");
		b.append(subs.size()).color(DARK_GRAY).append(" available subcommands");
		if( (page+1)*6<subs.size() )  b.appendButton("Prev Page", alias+" -h " + (page+1));
		b.send(s);
	}
}
