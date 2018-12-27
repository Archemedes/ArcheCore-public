package net.lordofthecraft.arche.command.brigadier;

import java.lang.reflect.Method;

import org.apache.commons.lang.Validate;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.mojang.brigadier.CommandDispatcher;

import lombok.Getter;
import lombok.var;
import net.lordofthecraft.arche.CoreLog;

public class BrigadierProvider {
	@Getter private boolean functional = true;

	private Method getServer;
	private Method getMCDispatcher;
	private Method getBrigadier;
	
	public BrigadierProvider() {
		try {
			var serverClass = MinecraftReflection.getMinecraftServerClass();
			getServer = serverClass.getMethod("getServer");
			getMCDispatcher = serverClass.getDeclaredMethod("getCommandDispatcher");
			getMCDispatcher.setAccessible(true);
			getBrigadier = reflectBrigadierGetter();
			
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
	

}
