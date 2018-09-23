package net.lordofthecraft.arche.listener;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.BlockRegistry;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class BlockRegistryListener implements Listener {
	private final BlockRegistry registry;
	private final ArcheTimer debug;
	
	public BlockRegistryListener(BlockRegistry registry) {
		this.registry = registry;
		debug = ArcheCore.getPlugin().getMethodTimer();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent e){
		if(debug != null) debug.startTiming("BlockRegistry place");
		if(e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		if(registry.isWatched(e.getBlock().getType())){
			registry.monitorBlock(e.getBlock());
		}
		if(debug != null) debug.stopTiming("BlockRegistry place");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onLiquid(BlockFromToEvent e){
		Block b = e.getToBlock();
		if(registry.isWatched(b.getType())){
			registry.removeBlock(b);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent e){
		if(debug != null) debug.startTiming("BlockRegistry break");
		Block b = e.getBlock();
		if(registry.isWatched(b.getType())){
			registry.removeBlock(b);
		}
		if(debug != null) debug.stopTiming("BlockRegistry break");
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBurn(BlockBurnEvent e){
		Block b = e.getBlock();
		if(registry.isWatched(b.getType())){
			registry.removeBlock(b);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPhysics(BlockPhysicsEvent e){
		Block b = e.getBlock();
		Material m = b.getType();
		if(registry.isWatched(m)){
			switch(m){
			case CACTUS:
				if(b.getRelative(BlockFace.EAST).getType().isSolid() || b.getRelative(BlockFace.SOUTH).getType().isSolid()
						|| b.getRelative(BlockFace.NORTH).getType().isSolid() || b.getRelative(BlockFace.WEST).getType().isSolid()
						|| (b.getRelative(BlockFace.DOWN).getType() != Material.CACTUS && b.getRelative(BlockFace.DOWN).getType() != Material.SAND) ){
					registry.removeBlock(b);
				}
				break;
				case TALL_GRASS:
				case DEAD_BUSH:
				case DANDELION:
				case POPPY:
				case BROWN_MUSHROOM:
				case RED_MUSHROOM:
				case SPRUCE_SAPLING:
				case ACACIA_SAPLING:
				case BIRCH_SAPLING:
				case DARK_OAK_SAPLING:
				case JUNGLE_SAPLING:
				case OAK_SAPLING:
				case CARROT:
				case POTATO:
				case MELON_STEM:
				case PUMPKIN_STEM:
				case SUGAR_CANE:
				case LARGE_FERN:
				case WHEAT:
				Material x = b.getRelative(BlockFace.DOWN).getType();
				if(x != Material.FARMLAND && x != Material.DIRT && x != Material.GRASS) registry.removeBlock(b);
				break;
			default: break;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBoom(EntityExplodeEvent e){
		if(debug != null) debug.startTiming("BlockRegistry explode");
		for(Block b : e.blockList()){
			if(registry.isWatched(b.getType())){
				registry.removeBlock(b);
			}
		}
		if(debug != null) debug.stopTiming("BlockRegistry explode");
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPistonPush(BlockPistonExtendEvent e){
		if(debug != null) debug.startTiming("BlockRegistry piston");
		List<Block> todo = Lists.newArrayList();

		//I hear spigot build 1.8 gets direction wrong sometimes.

		for(Block b : e.getBlocks()){
			if(registry.isWatched(b.getType())){
				if(registry.isPlayerPlaced(b)) {
					//registry.removeBlock(b); Temp hyper-aggressive registry placement to counteract Bukkit bug
					if(b.getPistonMoveReaction() == PistonMoveReaction.MOVE){
						todo.add(b.getRelative(e.getDirection()));
						registry.removeBlock(b);
					}else if(b.getPistonMoveReaction() == PistonMoveReaction.BREAK){
						registry.removeBlock(b);
					}
				}  //else registry.removeBlock(b.getRelative(e.getDirection())); Temp hyper-aggressive registry placement to counteract Bukkit bug
			}
		}
		
		for(Block b : todo){
			registry.monitorBlock(b);
		}
		if(debug != null) debug.stopTiming("BlockRegistry piston");
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPistonPull(BlockPistonRetractEvent e){
		if(debug != null) debug.startTiming("BlockRegistry piston");
		if(e.isSticky()){
			List<Block> todo = Lists.newArrayList();
			
			for(Block b : e.getBlocks()){
				if(registry.isWatched(b.getType())){
					if(registry.isPlayerPlaced(b)) {
						//registry.removeBlock(b); Temp hyper-aggressive registry placement to counteract Bukkit bug
						if(b.getPistonMoveReaction() == PistonMoveReaction.MOVE){
							todo.add(b.getRelative(e.getDirection()));
							registry.removeBlock(b);
						}else if(b.getPistonMoveReaction() == PistonMoveReaction.BREAK){
							registry.removeBlock(b);
						}
					}  //else registry.removeBlock(b.getRelative(e.getDirection())); Temp hyper-aggressive registry placement to counteract Bukkit bug
				}
			}
			
			for(Block b : todo){
				registry.monitorBlock(b);
			}
		}
		if(debug != null) debug.stopTiming("BlockRegistry piston");
	}
	
}
