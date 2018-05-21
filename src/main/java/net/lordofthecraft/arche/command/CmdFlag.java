package net.lordofthecraft.arche.command;


import java.util.Arrays;
import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;


@FieldDefaults(level=AccessLevel.PRIVATE)
@Getter
public class CmdFlag {
	final String name;
	final List<String> aliases;
	@Setter CmdArg<?> arg;
	
	private CmdFlag(String name, String... flagAliases){
		this.name = name;
		aliases = Arrays.asList(flagAliases);
	}
	
	public static ArgBuilder make(ArcheCommandBuilder target, String name, String... flagAliases) {
		CmdFlag flag = new CmdFlag(name, flagAliases);
		target.addFlag(flag);
		ArgBuilder builder = new ArgBuilder(target);
		return builder;
	}
	
	
}
