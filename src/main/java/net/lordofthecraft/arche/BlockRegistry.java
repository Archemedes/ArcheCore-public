package net.lordofthecraft.arche;

import com.google.common.collect.Sets;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.logging.BlockRegistryDeleteTask;
import net.lordofthecraft.arche.save.tasks.logging.BlockRegistryInsertTask;
import net.lordofthecraft.arche.util.WeakBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.EnumSet;
import java.util.Set;

public class BlockRegistry {
	final Set<WeakBlock> playerPlaced = Sets.newHashSetWithExpectedSize(3000);
	private final Set<Material> watching = EnumSet.noneOf(Material.class);
    private final SaveHandler buffer = SaveHandler.getInstance();

    BlockRegistry() {
    }

    /**
	 * Lets you specify the material which the registry will watch for player placement
	 * @param m The material of the blocks to watch
	 */
	public void addWatchedMaterial(Material m){
		watching.add(m);
	}
	
	/**
	 * See if the Player placement Registry keeps track of blocks of a certain material
	 * @param m The material to check
	 * @return Whether the material is kept track of
	 */
	public boolean isWatched(Material m){
		return watching.contains(m);
	}
	
	/**
	 * Add a specific block to the list of watched blocks
	 * @param b the block to monitor
	 */
	public void monitorBlock(Block b){
		WeakBlock wb = new WeakBlock(b);
		playerPlaced.add(wb);
        buffer.put(new BlockRegistryInsertTask(wb));
    }

    /**
	 * No longer monitor this particular block
	 * @param b The block to alter
	 * @return whether or not the block was being monitored
	 */
	public boolean removeBlock(Block b){
		WeakBlock wb = new WeakBlock(b);
		boolean res = playerPlaced.remove(wb);
        buffer.put(new BlockRegistryDeleteTask(wb));
        return res;
    }
	
	/**
	 * Check if the block in question was recently placed by a player
	 * @param b The Block to check for
	 * @return Whether or not the block was player-placed
	 */
	public boolean isPlayerPlaced(Block b){
		return playerPlaced.contains(new WeakBlock(b));
	}
	
}
