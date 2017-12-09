package net.lordofthecraft.arche.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import net.lordofthecraft.arche.seasons.LotcianCalendar;
import net.lordofthecraft.arche.seasons.Season;

public class SeasonListener implements Listener {
	private final LotcianCalendar calendar;
	
	public SeasonListener(LotcianCalendar calendar) {
		this.calendar = calendar;
	}
	
	@EventHandler
	public void world(WorldLoadEvent e) {
		calendar.getRunnable().loadWorld(e.getWorld());
	}
	
	@EventHandler
	public void world(WorldUnloadEvent e) {
		calendar.getRunnable().unloadWorld(e.getWorld());
	}
	
	@EventHandler(ignoreCancelled = true)
    public void rain(final WeatherChangeEvent e) {
		boolean weatherState = e.toWeatherState();
		
		if(weatherState && calendar.getSeason() == Season.SUMMER && Math.random() > 0.25)
			e.setCancelled(true);
	}
	
}
