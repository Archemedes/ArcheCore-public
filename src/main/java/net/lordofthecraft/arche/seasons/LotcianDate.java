package net.lordofthecraft.arche.seasons;

import java.text.ChoiceFormat;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;

public final class LotcianDate implements Comparable<LotcianDate> {
	public static final int DAYS_PER_MONTH = 24;
	public static final int MONTHS_PER_YEAR = 7;
	public static final int DAYS_PER_YEAR = DAYS_PER_MONTH * MONTHS_PER_YEAR;
	
    private static final ChoiceFormat GRAMMAR = new ChoiceFormat("1#st | 2#nd | 3#rd | 3<th | 21#st | 22#nd | 23#rd | 23<th ");
	
	private final int year;
	private final Month month;
	private final int day;
	
	public static LotcianDate fromTag(String tagvalue) {
		String[] parts = tagvalue.split("@");
		int year = Integer.parseInt(parts[0]);
		Month month = Month.values()[Integer.parseInt(parts[1])];
		int day = Integer.parseInt(parts[2]);
		
		return new LotcianDate(year, month, day);
	}
	
	public LotcianDate(int year, Month month, int day) {
		Validate.isTrue(day > 0 && day <= DAYS_PER_MONTH, "Tried to make calendar with illegal day: " + day);
		
		this.year = year;
		this.month = month;
		this.day = day;
	}
	
	public int getYear() {
		return year;
	}
	
	public Month getMonth() {
		return month;
	}
	
	public int getDay() {
		return day;
	}
	
	private int dayNumberSinceOrigin() {
		return (year * DAYS_PER_YEAR) + (month.ordinal() * DAYS_PER_MONTH) + day;
	}
	
	public String asTagValue() {
		return String.join("@", 
				Integer.toString(year), 
				Integer.toString(month.ordinal()), 
				Integer.toString(day));
	}
	
	@Override
	public int hashCode() {
		return dayNumberSinceOrigin();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null || other.getClass() != this.getClass())
			return false;
		
		LotcianDate date = (LotcianDate) other;
		
		return year == date.year && month == date.month && day == date.day;
	}
	
	@Override
	public String toString() {
		 return "LotcianDate=" + this.year + "-" + this.month + "-" + this.day;
	}
	
    public String toPrettyString() {
    	return ChatColor.DARK_AQUA + "" + day + GRAMMAR.format(day) 
    	+ ChatColor.GRAY + "of " + ChatColor.AQUA + ChatColor.ITALIC + month.getName()
    	+ ChatColor.GRAY + ", " + ChatColor.WHITE + year + ChatColor.GRAY + ".";
    }
	
	@Override
	public int compareTo(LotcianDate other) {
		return this.dayNumberSinceOrigin() - other.dayNumberSinceOrigin();
	}
}
