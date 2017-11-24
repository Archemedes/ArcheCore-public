package net.lordofthecraft.arche.seasons;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MonthChangeEvent extends Event
{
    private static final HandlerList handlers;

    static {
        handlers = new HandlerList();
    }

    private Month month;
    private int year;
    
    public MonthChangeEvent(final Month month, final int year) {
        super();
        this.month = month;
        this.year = year;
    }

    public static HandlerList getHandlerList() {
        return MonthChangeEvent.handlers;
    }
    
    public Month getMonth() {
        return this.month;
    }
    
    public int getYear() {
        return this.year;
    }
    
    public HandlerList getHandlers() {
        return MonthChangeEvent.handlers;
    }
}
