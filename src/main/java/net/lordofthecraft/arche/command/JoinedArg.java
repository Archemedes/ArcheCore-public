package net.lordofthecraft.arche.command;

import java.util.List;

public class JoinedArg extends CmdArg<String> {

	public JoinedArg(String name, String errorMessage, String defaultInput, String description) {
		super(name, errorMessage, defaultInput, description);
		this.setMapper(s->s);
	}

	@Override
	String resolve(List<String> input, int i) {
		List<String> relevantInput = input.subList(i, input.size());
		String joined = String.join(" ", relevantInput);
		return resolve(joined);
	}
	
}
