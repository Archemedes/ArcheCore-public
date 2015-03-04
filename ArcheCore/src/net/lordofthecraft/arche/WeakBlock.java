package net.lordofthecraft.arche;

import org.bukkit.block.*;
import org.bukkit.*;
import org.apache.commons.lang.*;

public class WeakBlock
{
    private final String world;
    private final int x;
    private final int y;
    private final int z;
    
    public WeakBlock(final World world, final int x, final int y, final int z) {
        super();
        this.world = world.getName();
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public WeakBlock(final String world, final int x, final int y, final int z) {
        super();
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public WeakBlock(final Block b) {
        this(b.getWorld(), b.getX(), b.getY(), b.getZ());
    }
    
    public WeakBlock(final Location location) {
        this(location.getBlock());
    }
    
    public String getWorld() {
        return this.world;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public int getZ() {
        return this.z;
    }
    
    @Override
    public int hashCode() {
        return (this.y << 24 ^ this.x ^ this.z) + ((this.world == null) ? 0 : (31 * this.world.hashCode()));
    }
    
    public Location toLocation() {
        final World w = Bukkit.getWorld(this.getWorld());
        final int x = this.getX();
        final int y = this.getY();
        final int z = this.getZ();
        return new Location(w, (double)x, (double)y, (double)z);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof WeakBlock)) {
            return false;
        }
        final WeakBlock other = (WeakBlock)o;
        return this.x == other.x && this.y == other.y && this.z == other.z && ObjectUtils.equals((Object)this.world, (Object)other.world);
    }
}
