package net.lordofthecraft.arche.command.brigadier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.var;
import net.lordofthecraft.arche.CoreLog;

public class BrigadierProvider {
	@Getter private boolean functional = true;

	private Method getServer;
	private Method getMCDispatcher;
	private Method getBrigadier;
	private Constructor<?> makeCommandWrapper;
	
	public BrigadierProvider() {
		try {
			var serverClass = MinecraftReflection.getMinecraftServerClass();
			getServer = serverClass.getMethod("getServer");
			getMCDispatcher = serverClass.getDeclaredMethod("getCommandDispatcher");
			getMCDispatcher.setAccessible(true);
			getBrigadier = reflectBrigadierGetter();
			
      var wrapperClass = MinecraftReflection.getCraftBukkitClass("command.BukkitCommandWrapper");
      makeCommandWrapper = wrapperClass.getConstructor(Bukkit.getServer().getClass(), Command.class);
		} catch(Exception e) {
			CoreLog.severe("We were unable to set up the BrigadierProvider. Likely a reflection error!");
			functional = false;
			e.printStackTrace();
		}
	}
	
	private Method reflectBrigadierGetter() throws Exception {
		var dispatcherClass = MinecraftReflection.getMinecraftClass("CommandDispatcher");
		for(var xx : dispatcherClass.getDeclaredMethods()) {
			if(xx.getParameterCount() == 0 && CommandDispatcher.class.isAssignableFrom(xx.getReturnType()))
				return xx;
		}
		
		throw new NoSuchMethodError("Fuck");
	}
	
	@SuppressWarnings("unchecked")
	public CommandDispatcher<Object> getBrigadier() {
		Validate.isTrue(functional);
		try {
			var server = getServer.invoke(null); //Static MinecraftServer getter
			var dispatch = getMCDispatcher.invoke(server);
			return (CommandDispatcher<Object>) getBrigadier.invoke(dispatch);
		} catch(Exception e) {
			CoreLog.severe("Brigadier Decided to crash on us after startup time");
			functional = false;
			return null;
		}
	}
	
	@SuppressWarnings("rawtypes")
	@SneakyThrows
	public SuggestionProvider suggestions(Command command) {
		return (SuggestionProvider) makeCommandWrapper.newInstance(Bukkit.getServer(), command);
	}
	

}
