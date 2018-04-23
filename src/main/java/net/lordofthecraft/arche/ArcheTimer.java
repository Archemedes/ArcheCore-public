package net.lordofthecraft.arche;

import com.google.common.collect.Maps;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class ArcheTimer {
	private final Logger logger;
	private final Map<String, Long> timings = Maps.newConcurrentMap();
	
	ArcheTimer(Plugin plugin){
		logger = plugin.getLogger();
	}
	
	public void startTiming(String why){
		long time = System.nanoTime();
		Validate.notNull(why);
		timings.put(why, time);
	}
	
	public void stopTiming(String why){
		long time = System.nanoTime();
		
		Validate.notNull(why);
		
		if(timings.containsKey(why)){
			long took = (time - timings.get(why))/1000;
			CoreLog.debug("operation '" + why + "' took " + took + "μs");
		}
	}
	
	public void stopAllTiming(){
		long time = System.nanoTime();
		for(Entry<String, Long> t : timings.entrySet())
			CoreLog.debug("timed action '" + t.getKey() + "' taking " + (time - t.getValue())/1000 + "μs");
		
		timings.clear();
	}	
}
