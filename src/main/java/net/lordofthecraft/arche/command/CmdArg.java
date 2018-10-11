package net.lordofthecraft.arche.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.base.Supplier;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
public class CmdArg<T> {
	@Setter private Function<String, T> mapper;
	@Setter private Predicate<T> filter = $->true;
	@Setter private Supplier<Collection<String>> completer = ArrayList::new;
	
	private final String name, errorMessage, defaultInput, description;
	
	T resolveDefault() {
		if(defaultInput == null) return null;
		return resolve(defaultInput);
	}
	
	T resolve(List<String> input, int i) {
		return resolve(input.get(i));
	}
	
	T resolve(String input) {
		T mapped = mapper.apply(input);
		if(mapped == null || !filter.test(mapped)) return null;
		
		return mapped;
	}
	
	void completeMe(String... opts) {
		setCompleter(()->Arrays.asList(opts));
	}
	
	void playerCompleter() {
		setCompleter(()->Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()) );
	}
	
	public boolean hasDefaultInput() {
		return defaultInput != null;
	}
	
	public boolean hasDescription() {
		return description != null;
	}
}
