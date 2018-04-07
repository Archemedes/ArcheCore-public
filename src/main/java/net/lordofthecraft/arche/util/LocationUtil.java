package net.lordofthecraft.arche.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class LocationUtil {
	private LocationUtil() {}

	public static boolean isClose(Entity e, Location l) {
		return isClose(e, l, 5);
	}
	
	public static boolean isClose(Entity e, Location l, double maxDist) {
		return isClose(e.getLocation(), l, maxDist);
	}
	
	public static boolean isClose(Entity e1, Entity e2) {
		return isClose(e1, e2, 5);
	}
	
	public static boolean isClose(Entity e1, Entity e2, double maxDist) {
		return isClose(e1.getLocation(), e2.getLocation(), maxDist);
	}
	
	public static boolean isClose(Location l1, Location l2) {
		return isClose(l1, l2, 5);
	}
	
	public static boolean isClose(Location l1, Location l2, double maxDist) {
		return l1.getWorld() == l2.getWorld() && l1.distance(l2) <= maxDist;
	}
}
