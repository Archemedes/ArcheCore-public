package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.*;
import org.bukkit.event.*;
import org.bukkit.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.block.*;
import com.google.common.collect.*;
import org.bukkit.block.*;
import java.util.*;

public class BlockRegistryListener implements Listener
{
    private final BlockRegistry registry;
    private final ArcheTimer debug;
    
    public BlockRegistryListener(final BlockRegistry registry) {
        super();
        this.registry = registry;
        this.debug = ArcheCore.getPlugin().getMethodTimer();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(final BlockPlaceEvent e) {
        if (this.debug != null) {
            this.debug.startTiming("BlockRegistry place");
        }
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            return;
        }
        if (this.registry.isWatched(e.getBlock().getType())) {
            this.registry.monitorBlock(e.getBlock());
        }
        if (this.debug != null) {
            this.debug.stopTiming("BlockRegistry place");
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLiquid(final BlockFromToEvent e) {
        final Block b = e.getToBlock();
        if (this.registry.isWatched(b.getType())) {
            this.registry.removeBlock(b);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent e) {
        if (this.debug != null) {
            this.debug.startTiming("BlockRegistry break");
        }
        final Block b = e.getBlock();
        if (this.registry.isWatched(b.getType())) {
            this.registry.removeBlock(b);
        }
        if (this.debug != null) {
            this.debug.stopTiming("BlockRegistry break");
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBurn(final BlockBurnEvent e) {
        final Block b = e.getBlock();
        if (this.registry.isWatched(b.getType())) {
            this.registry.removeBlock(b);
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPhysics(final BlockPhysicsEvent e) {
        final Block b = e.getBlock();
        final Material m = b.getType();
        if (this.registry.isWatched(m)) {
            switch (m) {
                case CACTUS: {
                    if (b.getRelative(BlockFace.EAST).getType().isSolid() || b.getRelative(BlockFace.SOUTH).getType().isSolid() || b.getRelative(BlockFace.NORTH).getType().isSolid() || b.getRelative(BlockFace.WEST).getType().isSolid() || (b.getRelative(BlockFace.DOWN).getType() != Material.CACTUS && b.getRelative(BlockFace.DOWN).getType() != Material.SAND)) {
                        this.registry.removeBlock(b);
                        break;
                    }
                    break;
                }
                case LONG_GRASS:
                case DEAD_BUSH:
                case YELLOW_FLOWER:
                case RED_ROSE:
                case BROWN_MUSHROOM:
                case RED_MUSHROOM:
                case SAPLING:
                case CARROT:
                case POTATO:
                case MELON_STEM:
                case PUMPKIN_STEM:
                case SUGAR_CANE_BLOCK:
                case DOUBLE_PLANT:
                case CROPS: {
                    final Material x = b.getRelative(BlockFace.DOWN).getType();
                    if (x != Material.SOIL && x != Material.DIRT && x != Material.GRASS) {
                        this.registry.removeBlock(b);
                        break;
                    }
                    break;
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBoom(final EntityExplodeEvent e) {
        if (this.debug != null) {
            this.debug.startTiming("BlockRegistry explode");
        }
        for (final Block b : e.blockList()) {
            if (this.registry.isWatched(b.getType())) {
                this.registry.removeBlock(b);
            }
        }
        if (this.debug != null) {
            this.debug.stopTiming("BlockRegistry explode");
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonPush(final BlockPistonExtendEvent e) {
        if (this.debug != null) {
            this.debug.startTiming("BlockRegistry piston");
        }
        final List<Block> todo = Lists.newArrayList();
        for (final Block b : e.getBlocks()) {
            if (this.registry.isWatched(b.getType()) && this.registry.isPlayerPlaced(b)) {
                if (b.getPistonMoveReaction() == PistonMoveReaction.MOVE) {
                    todo.add(b.getRelative(e.getDirection()));
                    this.registry.removeBlock(b);
                }
                else {
                    if (b.getPistonMoveReaction() != PistonMoveReaction.BREAK) {
                        continue;
                    }
                    this.registry.removeBlock(b);
                }
            }
        }
        for (final Block b : todo) {
            this.registry.monitorBlock(b);
        }
        if (this.debug != null) {
            this.debug.stopTiming("BlockRegistry piston");
        }
    }
}
