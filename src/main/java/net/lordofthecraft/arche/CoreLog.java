package net.lordofthecraft.arche;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CoreLog {

	private CoreLog() {}
	
	
	public static void log(Level level, String msg) {
		get().log(level, msg);
	}
	
	public static void log(Level level, String msg, Throwable thrown) {
		get().log(level, msg, thrown);
	}
	
	public static void severe(String msg) {
		get().severe(msg);
	}
	
	public static void warning(String msg) {
		get().warning(msg);
	}
	
	public static void info(String msg) {
		get().info(msg);
	}
	
	public static void debug(String msg) {
		get().fine(msg);
	}
	
	private static Logger get() {
		return ArcheCore.getPlugin().getLogger();
	}
	
}
