package net.lordofthecraft.arche.command;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;

import com.google.common.base.Supplier;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lordofthecraft.arche.interfaces.OfflinePersona;

@RequiredArgsConstructor
@Accessors(fluent=true)
@Setter
public class ArgTypeTemplate<T> {
	private final Class<T> forClass;
	private Function<String, T> mapper;
	private Predicate<T> filter;
	private Supplier<Collection<String>> completer;
	
	
	CmdArg<T> provide(String name, String errorMessage, String defaultInput, String description) {
		CmdArg<T> arg = new CmdArg<>(name, errorMessage, defaultInput, description);
		if(completer != null) arg.setCompleter(completer);
		if(filter != null) arg.setFilter(filter);
		if(mapper != null) arg.setMapper(mapper);
		return arg;
	}
	
	public void register() {
		Validate.notNull(forClass, "There is no class specified for this argument type");
		Validate.isTrue(isClassValid(), "The class to specify as an argument type was already handled");
		//XXX
	}
	
	private boolean isClassValid() {
		if(forClass.isEnum()) return false;
		if(OfflinePersona.class.isAssignableFrom(forClass)) return false;
		if(OfflinePlayer.class.isAssignableFrom(forClass)) return false;
		if(forClass.isPrimitive()) return false;
		if(forClass == Integer.class) return false;
		if(forClass == Long.class) return false;
		if(forClass == Float.class) return false;
		if(forClass == Double.class) return false;
		if(forClass == String.class) return false;
		if(forClass == Boolean.class) return false;
		
		return true;
	}
	
}
