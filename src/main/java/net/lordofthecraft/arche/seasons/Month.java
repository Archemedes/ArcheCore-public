package net.lordofthecraft.arche.seasons;

import org.bukkit.ChatColor;

public enum Month
{
    SNOWMAIDEN("Snow's Maiden", ChatColor.BLUE, "The snow of the Deep Cold breaks, leaving a slight chill in the air as time steps into\nthe Winter month of ", Season.WINTER), 
    FIRSTSEED("The First Seed", ChatColor.DARK_GREEN, "plants begin to grow once more as the gripping cold departs the world, harbinging Spring;\nthe month is ", Season.SPRING), 
    MALINWELCOME("Malin's Welcome", ChatColor.GOLD, "The world seems to smile as the heat of the Summer months begins and the world prospers.\nThis is the Summer month of ", Season.SUMMER), 
    GRANDHARVEST("The Grand Harvest", ChatColor.GOLD, "The seeds from the former months have grown. The fields are bountiful and the sky is clear.\nThis is the Summer month of ", Season.SUMMER), 
    SUNSMILE("Sun's Smile", ChatColor.GOLD, "As summer comes to a close the sun presents a delightful glow, giving its last light to illuminate the Summer month of ", Season.SUMMER), 
    AMBERCOLD("The Amber Cold", ChatColor.BLUE, "The skies have turned to gray and a chill travels on the wind.\nThis is the Autumn month of ", Season.AUTUMN), 
    DEEPCOLD("The Deep Cold", ChatColor.BLUE, "Winter is at it's harshest, winds howl and blow as each day passes. Spring shall begin soon, but not soon enough.\nThis is ", Season.WINTER);
    
    private final String name;
    private final Season season;
    private final String greeting;
    private final ChatColor prefix;
    
    Month(final String name, final ChatColor prefix, final String greeting, final Season season) {
        this.name = name;
        this.season = season;
        this.prefix = prefix;
        this.greeting = greeting;
    }
    
    public static Month valueOf(int i) {
    	return Month.values()[i];
    }
    
    public String getName() {
        return this.name;
    }
    
    public Month getPrevious() {
        final int previous = ((this.ordinal() == 0) ? values().length : this.ordinal()) - 1;
        return values()[previous];
    }
    
    public int getOrder() {
        return this.ordinal();
    }
    
    public Season getSeason() {
        return this.season;
    }
    
    public String getGreeting() {
    	return this.greeting;
    }
    
    public ChatColor getPrefix() {
    	return this.prefix;
    }
    
    Month nextMonth() {
        final int next = this.ordinal() + 1;
        return values()[(next < values().length) ? next : 0];
    }
}
