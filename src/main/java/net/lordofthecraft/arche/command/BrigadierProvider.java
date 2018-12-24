package net.lordofthecraft.arche.command;

import com.comphenix.protocol.utility.MinecraftReflection;
import com.mojang.brigadier.CommandDispatcher;

import lombok.Getter;
import lombok.var;
import net.lordofthecraft.arche.CoreLog;

public class BrigadierProvider {
	@Getter private boolean functional = true;
	@Getter private final CommandDispatcher<?> brigadier;
	
	BrigadierProvider() {
		try {
			var server = MinecraftReflection.getMinecraftServerClass();
		
		} catch(Exception e) {
			CoreLog.severe("We were unable to set up the BrigadierProvider. Likely a reflection error!");
			functional = false;
			e.printStackTrace();
		}
	}
	

}
