package net.lordofthecraft.arche.util;

import static java.util.concurrent.TimeUnit.*;
import static org.bukkit.ChatColor.*;

import org.bukkit.ChatColor;

import net.md_5.bungee.api.chat.BaseComponent;

public final class TimeUtil {

	private TimeUtil() {}
	
	
	public static BaseComponent printTicks(long ticks) {
		return printMillis(ticks * 50l);
	}
	
	public static BaseComponent printTicksRaw(long ticks) {
		return printMillisRaw(ticks * 50l);
	}
	
	public static BaseComponent printMillis(long millis) {
		return print(millis, false, WHITE, GRAY);
	}
	
	public static BaseComponent printMillisRaw(long millis) {
		return print(millis, false, null, null);
	}
	
	public static BaseComponent printBrief(long millis) {
		return print(millis, true, null, null);
	}
	
	public static BaseComponent print(long ms, boolean brief, ChatColor numColor, ChatColor unitColor) {
		ChatBuilder sb = MessageUtil.builder();
		
		long days = MILLISECONDS.toDays(ms);
		long hours = MILLISECONDS.toHours(ms) - days*24;
		long minutes = MILLISECONDS.toMinutes(ms) - DAYS.toMinutes(days) - hours*60;
		long seconds = MILLISECONDS.toSeconds(ms) - DAYS.toSeconds(days) - hours*3600 - minutes*60;
		
		append(sb, days, brief, "days", "d", numColor, unitColor);
		append(sb, hours, brief, "hours", "h", numColor, unitColor);
		append(sb, minutes, brief, "minutes", "m", numColor, unitColor);
		append(sb, seconds, brief, "seconds", "s", numColor, unitColor);
		
		return sb.build();
	}

	private static void append(ChatBuilder sb, long val, boolean brief, String big, String small, ChatColor c1, ChatColor c2) {
		if(val == 0) return;
		
		sb.append(val);
		if(c1 != null) sb.color(c1);
		
		if(brief) sb.append(small);
		else sb.append(' ' + big + ' ');
		if(c2!=null) sb.color(c2);
	}
}