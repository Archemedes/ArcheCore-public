package net.lordofthecraft.arche.command;

import java.lang.reflect.Method;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.mojang.brigadier.CommandDispatcher;

import lombok.Getter;
import lombok.var;
import net.lordofthecraft.arche.CoreLog;

public class BrigadierProvider {
	@Getter private boolean functional = true;
	@Getter private CommandDispatcher<?> brigadier;

	private Method getServer;
	private Method getMCDispatcher;
	private Method getBrigadier;
	
	BrigadierProvider() {
		try {
			var serverClass = MinecraftReflection.getMinecraftServerClass();
			getServer = serverClass.getMethod("getServer");
			getMCDispatcher = serverClass.getDeclaredMethod("getCommandDispatcher");
			getMCDispatcher.setAccessible(true);
			getBrigadier = reflectBrigadierGetter();
			
			brigadier = reflectBrigadier();
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
	
	
	private CommandDispatcher<?> reflectBrigadier() throws Exception {
		var server = getServer.invoke(null); //Static MinecraftServer getter
		var dispatch = getMCDispatcher.invoke(server);
		return (CommandDispatcher<?>) getBrigadier.invoke(dispatch);
	}
	

}
