package net.lordofthecraft.arche.seasons;

import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Bukkit;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.event.MonthChangeEvent;

public class LotcianCalendar {
    private final CalendarRunnable runnable;
    private final BiomeSwitcher switcher;
    
    private LotcianDate date;
    private int daytick;
    
    public LotcianCalendar(List<String> trackedWorlds, boolean switchBiomes, int offsetYears) {
    	long millis = System.currentTimeMillis();
        millis += (DateUtils.MILLIS_PER_DAY/2);
        long MILLIS_PER_WEEK = 7 * DateUtils.MILLIS_PER_DAY;
        int year = (int)(millis / MILLIS_PER_WEEK) - 860;
        millis %= MILLIS_PER_WEEK;
        Month month = Month.valueOf((int)(millis / DateUtils.MILLIS_PER_DAY));
        millis %= DateUtils.MILLIS_PER_DAY;
        int day = (int)(millis / DateUtils.MILLIS_PER_HOUR) + 1;
        this.daytick = (int)(millis % DateUtils.MILLIS_PER_HOUR) / 150;
        if (this.daytick >= 21000 && ++day > LotcianDate.DAYS_PER_MONTH) {
            day = 1;
            month = month.nextMonth();
            if (month == Month.SNOWMAIDEN) {
                ++year;
            }
        }
        
        date = new LotcianDate(year, month, day);
        
        runnable = new CalendarRunnable(this,  millis, trackedWorlds);
        switcher = new BiomeSwitcher(ArcheCore.getPlugin(), this, switchBiomes);
    }
    
    public void onEnable() {
    	runnable.runTaskTimer(ArcheCore.getPlugin(), 43l, 7l);
    	switcher.startListening();
    }
    
    void increment(final long millis) {
    	int year = getYear();
    	Month month = getMonth();
    	int day = getDay();
    	
        final int newtick = this.daytick + (int)(millis / 150L);
        if (newtick >= 21000 && this.daytick < 21000) {
        	day += 1;
        	if(day > LotcianDate.DAYS_PER_MONTH) {
        		day = 1;
        		
        		CoreLog.info("Initiating a calendar month switch from " + month.getName() + " to " + month.nextMonth().getName());
        		CoreLog.info("Month switch caused by an increment of " + millis + " leading to daytick=" + newtick);
        		
        		if(month.getSeason() == Season.WINTER && month.nextMonth().getSeason() != Season.WINTER) {
        			switcher.setWinter(false);
        			runnable.refreshAllChunks();
        		} else if(month.getSeason() != Season.WINTER && month.nextMonth().getSeason() != Season.WINTER) {
        			switcher.setWinter(true);
        			runnable.refreshAllChunks();
        		}

        		month = month.nextMonth();
        		if (month == Month.SNOWMAIDEN) ++year;
        		
        		final MonthChangeEvent event = new MonthChangeEvent(month, year);
        		Bukkit.getPluginManager().callEvent(event);
        	}
        	
        	date = new LotcianDate(year, month, day);
        }
        this.daytick = newtick;
        if (this.daytick >= 24000) {
            this.daytick %= 24000;
        }
    }
    
    public CalendarRunnable getRunnable() {
    	return runnable;
    }
    
    public int getMinecraftDayClock() {
        return this.daytick;
    }
    
    public LotcianDate getDate() {
    	return date;
    }
    
    public int getDay() {
        return date.getDay();
    }
    
    //used for internal day elongation calculations in CalendarRunnable;
    int getMeteorologicalDay() {
    	if(daytick >= 21000) {
    		return getDay()-1; //Yes it must return 0 if day == 1
    	} else {
    		return getDay();
    	}
    }
    
    public Month getMonth() {
        return date.getMonth();
    }
    
    public Season getSeason() {
        return date.getMonth().getSeason();
    }
    
    public int getYear() {
        return date.getYear();
    }
    
    @Override
    public String toString() {
        return "LotcianCalendar Date: {" + date.toString() + "} daytick: " + this.daytick;
    }
    
    public String toPrettyString() {
        return date.toPrettyString();
    }
}
