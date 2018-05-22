package net.lordofthecraft.arche.command;

import java.util.function.Function;
import java.util.function.Predicate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class CmdArg<T> {
	@Setter private Function<String, T> mapper; 
	@Setter private Predicate<T> filter = $->true;
	
	private final String name, errorMessage, defaultInput;
	
	T resolveDefault() {
		if(defaultInput == null) return null;
		return resolve(defaultInput);
	}
	
	T resolve(String input) {
		T mapped = mapper.apply(input);
		if(mapped == null || !filter.test(mapped)) return null;
		
		return mapped;
	}
	
	public boolean hasDefaultInput() {
		return defaultInput != null;
	}
}
