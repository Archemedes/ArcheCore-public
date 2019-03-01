package net.lordofthecraft.arche.seasons;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Maps;

public class CalendarRunnable extends BukkitRunnable
{
    private final LotcianCalendar calendar;
    private final Map<String, World> worlds;
    
    private long lastUpdate;
    
    CalendarRunnable( LotcianCalendar calendar, final long initTime, List<String> trackedWorlds) {
    	this.calendar = calendar;
    	this.lastUpdate = initTime;

    	final List<String> l = trackedWorlds;

    	this.worlds = Maps.newHashMap();
    	l.forEach(w->worlds.put(w, Bukkit.getWorld(w)));
    }
    
    public void loadWorld(World w) {
    	String name = w.getName();
    	if(worlds.containsKey(name)) {
    		worlds.put(name, w);
    	}
    }
    
    public void unloadWorld(World w) {
    	String name = w.getName();
    	if(worlds.containsKey(name)) {
    		worlds.put(name, null);
    	}
    }
    
    public Collection<String> getTrackedWorlds(){
    	return worlds.keySet();
    }
    
   @SuppressWarnings("deprecation")
	void refreshAllChunks() {
	   worlds.values().stream()
	   .filter(Objects::nonNull)
	   .forEach(w->Arrays.stream(w.getLoadedChunks())
			   .forEach(c->w.refreshChunk(c.getX(), c.getZ()))
			   );
   }
    
    @Override
    public void run() {
    	final long time = System.currentTimeMillis() + (DateUtils.MILLIS_PER_DAY/2);
    	final long dt = time - this.lastUpdate;
    	this.calendar.increment(dt);
    	this.lastUpdate = time;

    	List<World> myWorlds = worlds.values().stream()
    			.filter(Objects::nonNull)
    			.filter(w->w.getGameRuleValue(GameRule.DO_DAYLIGHT_CYCLE))
    			.collect(Collectors.toList());

    	if(myWorlds.isEmpty()) return;
    	final int toSet = this.elongateDay(this.calendar.getMinecraftDayClock());
    	
    	for(World w :  myWorlds) {
    		long fullTime = w.getFullTime();
    		int remainder = (int) (fullTime % 24000L);
    		if(remainder < 2000 && toSet > 22000) remainder += 24000L;
    		else if(remainder > 22000 & toSet < 2000) remainder -= 24000L;
    		
    		fullTime += toSet - remainder;
    		//fullTime += (toSet - remainder) + ((remainder > toSet) ? 24000 : 0);
    		w.setFullTime(fullTime);
    	}
    }
    
    private int elongateDay(final int mctick) {
        if (mctick > 22000) {
            return mctick;
        }
        final double m = this.multiplier();
        if (mctick < 12000.0 * m) {
            return (int)(mctick / m);
        }
        if (mctick < 12000.0 * m + 2000.0) {
            return 12000 + (int)(mctick - 12000.0 * m);
        }
        return (int)(22000.0 - (22000.0 - mctick) / (2.5 - 1.5 * m));
    }
    
    private double multiplier() {
    	//Multiplier is the factor by which to extend the daytime ticks
    	//Specifically, the ticks between 0-12000.
    	//Nighttime ticks are contracted to compensate
    	//So multiplier must be smaller than 5/3
        switch (this.calendar.getMonth()) {
        case SNOWMAIDEN:
        	return 0.5 + 0.75 * (calendar.getMeteorologicalDay() / 24.0);
        case FIRSTSEED:
        	return 1.25 + 0.25 * (calendar.getMeteorologicalDay() / 24.0);
        case MALINWELCOME:
        case GRANDHARVEST:
        	return 1.5;
        case SUNSMILE:
        	return 1.25 + 0.25 * (1.0 - calendar.getMeteorologicalDay() / 24.0);
        case AMBERCOLD:
        	return 0.5 + 0.75 * (1.0 - calendar.getMeteorologicalDay() / 24.0);
        case DEEPCOLD:
        	return 0.5;
        default: throw new IllegalArgumentException();
        }
    }
}
