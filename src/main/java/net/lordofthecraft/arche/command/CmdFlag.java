package net.lordofthecraft.arche.command;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;


@FieldDefaults(level=AccessLevel.PRIVATE)
@Getter
public class CmdFlag {
	final String name;
	final Set<String> aliases;
	final String pex;
	@Setter CmdArg<?> arg;
	
	private CmdFlag(String name, String pex, String... flagAliases){
		this.name = name;
		this.pex = pex;
		Set<String> als = new HashSet<>();
		als.add(name);
		als.addAll(Arrays.asList(flagAliases));
		aliases = Collections.unmodifiableSet(als);
	}
	
	public static ArgBuilder make(ArcheCommandBuilder target, String name, String... flagAliases) {
		return make(target, name, null, flagAliases);
	}
	
	public static ArgBuilder make(ArcheCommandBuilder target, String name, String pex, String... flagAliases) {
		CmdFlag flag = new CmdFlag(name, pex, flagAliases);
		if(flag.collidesWithAny(target.flags())) throw new IllegalStateException("Your flag aliases are overlapping for command: " + target.mainCommand());
		
		target.addFlag(flag);
		ArgBuilder builder = new ArgBuilder(target);
		return builder;
	}
	
	public boolean collidesWithAny(List<CmdFlag> flags) {
		return flags.stream().anyMatch(this::collidesWith);
	}
	
	public boolean collidesWith(CmdFlag flag) {
		for(String alias : aliases) {
			if(flag.getAliases().stream().anyMatch(alias::equals)) return true;	
		}
		return false;
	}
	
	boolean mayUse(CommandSender s) {
		return StringUtils.isEmpty(pex) || s.hasPermission(pex);
	}
	
}
