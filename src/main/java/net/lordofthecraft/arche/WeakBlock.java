package net.lordofthecraft.arche;

import org.apache.commons.lang.ObjectUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * Represents a block that does not keep strong references to any CraftBukkit or Minecraft objects, allowing for
 * worry-free storage of them into Collections.
 */
public class WeakBlock {
	private final String world;
	private final int x,y,z;
	
	public WeakBlock(World world, int x, int y, int z){
		this.world = world.getName();
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public WeakBlock(String world, int x, int y, int z){
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public WeakBlock(Block b){
		this(b.getWorld(), b.getX(), b.getY(), b.getZ());
	}
	
	public WeakBlock(Location location) {
		this(location.getBlock());
	}

	public String getWorld(){return world;}
	public int getX(){return x;}
	public int getY(){return y;}
	public int getZ(){return z;}
	
	@Override
	public int hashCode(){
		return (this.y << 24 ^ this.x ^ this.z) + (world == null? 0 : 31 * world.hashCode());
	}
	
	public Location toLocation(){
		World w = Bukkit.getWorld(getWorld());
		int x = getX();
		int y = getY();
		int z = getZ();
		return new Location(w, x, y, z);
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof WeakBlock)) return false;
		WeakBlock other = (WeakBlock) o;
		
		return this.x == other.x && this.y == other.y && this.z == other.z && ObjectUtils.equals(this.world, other.world);
	}
	
	
}
