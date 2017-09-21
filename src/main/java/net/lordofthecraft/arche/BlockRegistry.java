package net.lordofthecraft.arche;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.lordofthecraft.arche.interfaces.IConsumer;
import net.lordofthecraft.arche.save.archerows.logging.BlockRegisteryRow;
import net.lordofthecraft.arche.save.archerows.logging.DelBlockRegistryRow;
import net.lordofthecraft.arche.util.WeakBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class BlockRegistry {
	final Set<WeakBlock> playerPlaced = Sets.newHashSetWithExpectedSize(3000);
	private final Set<Material> watching = EnumSet.noneOf(Material.class);
    final Map<WeakBlock, String> data = Maps.newConcurrentMap();
    //private final ArcheExecutor buffer = ArcheExecutor.getInstance();
    private final IConsumer consumer = ArcheCore.getControls().getConsumer();

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
        consumer.queueRow(new BlockRegisteryRow(wb));
        //buffer.put(new BlockRegistryInsertTask(wb));
    }

    /**
     * Add a specific block with a String of plugin-specified data to the Block Registry.
     *
     * @param b    The block to monitor
     * @param data The data to add to this weak location
     */
    public void monitorBlock(Block b, String data) {
        WeakBlock wb = new WeakBlock(b);
        playerPlaced.add(wb);
        this.data.putIfAbsent(wb, data);
        consumer.queueRow(new BlockRegisteryRow(wb, data));
    }

    /**
	 * No longer monitor this particular block
	 * @param b The block to alter
	 * @return whether or not the block was being monitored
	 */
	public boolean removeBlock(Block b){
		WeakBlock wb = new WeakBlock(b);
		boolean res = playerPlaced.remove(wb);
        //buffer.put(new BlockRegistryDeleteTask(wb));
        consumer.queueRow(new DelBlockRegistryRow(wb));
        if (data.containsKey(wb)) {
            data.remove(wb);
        }
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

    /**
     * Obtain potential data that is attached to this block, a simple string.
     *
     * @param b The block to check against
     * @return The data wrapped in an {@link Optional}
     */
    public Optional<String> getData(Block b) {
        WeakBlock wb = new WeakBlock(b);
        return data.entrySet().parallelStream().filter(ent -> ent.getKey().equals(wb)).map(Map.Entry::getValue).findFirst();
    }
}
