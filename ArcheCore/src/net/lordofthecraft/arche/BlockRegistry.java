package net.lordofthecraft.arche;

import org.bukkit.*;
import net.lordofthecraft.arche.save.*;
import com.google.common.collect.*;
import java.util.*;
import org.bukkit.block.*;
import net.lordofthecraft.arche.save.tasks.*;

public class BlockRegistry
{
    final Set<WeakBlock> playerPlaced;
    private final Set<Material> watching;
    private final SaveHandler buffer;
    
    BlockRegistry() {
        super();
        this.playerPlaced = Sets.newHashSetWithExpectedSize(3000);
        this.watching = EnumSet.noneOf(Material.class);
        this.buffer = SaveHandler.getInstance();
    }
    
    public void addWatchedMaterial(final Material m) {
        this.watching.add(m);
    }
    
    public boolean isWatched(final Material m) {
        return this.watching.contains(m);
    }
    
    public void monitorBlock(final Block b) {
        final WeakBlock wb = new WeakBlock(b);
        this.playerPlaced.add(wb);
        this.buffer.put(new BlockRegistryInsertTask(wb));
    }
    
    public boolean removeBlock(final Block b) {
        final WeakBlock wb = new WeakBlock(b);
        final boolean res = this.playerPlaced.remove(wb);
        this.buffer.put(new BlockRegistryDeleteTask(wb));
        return res;
    }
    
    public boolean isPlayerPlaced(final Block b) {
        return this.playerPlaced.contains(new WeakBlock(b));
    }
}
