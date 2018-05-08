package net.lordofthecraft.arche.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

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

	public static boolean isFloating(LivingEntity le) {
		return isInWater(le) || isInLava(le);
	}
	
	public static boolean isInLava(LivingEntity le) {
		Block b = le.getLocation().getBlock();
		return b.getType() == Material.LAVA || b.getType() == Material.STATIONARY_LAVA;
	}
	
	public static boolean isInWater(LivingEntity le) {
		Block b = le.getLocation().getBlock();
		return b.getType() == Material.STATIONARY_WATER || b.getType() == Material.WATER;
	}
}
