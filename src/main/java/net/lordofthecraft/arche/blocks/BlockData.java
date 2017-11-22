package net.lordofthecraft.arche.blocks;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.util.WeakBlock;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.sql.Timestamp;
import java.util.Map;

public final class BlockData {

    final WeakBlock block;
    private final Map<String, String> data;
    Timestamp dateCreated;
    private boolean decay = false;
    private boolean dropsOnBreak = true;

    public BlockData(WeakBlock block) {
        this.block = block;
        data = Maps.newConcurrentMap();
    }

    public BlockData(WeakBlock block, Map<String, String> data) {
        this.block = block;
        this.data = data;
    }

    public BlockData(Block b) {
        this.block = new WeakBlock(b);
        this.data = Maps.newConcurrentMap();
    }

    public BlockData(String world, int x, int y, int z, Map<String, String> data) {
        this.block = new WeakBlock(world, x, y, z);
        this.data = data;
    }

    public Timestamp getDateCreated() {
        return dateCreated;
    }

    public boolean hasKey(String key) {
        return data.containsKey(key);
    }

    public String getValue(String key) {
        return data.get(key);
    }

    public WeakBlock getBlock() {
        return block;
    }

    public Location getLocation() {
        return block.toLocation();
    }

    public boolean setValue(String key, String value) {
        boolean replace = data.containsKey(key);
        data.put(key, value);
        //TODO 501 save
        return replace;
    }

    public void removeValue(String key) {
        data.remove(key);
    }

    public boolean doesDecay() {
        return decay;
    }

    public void setDecay(boolean decay) {
        this.decay = decay;
        //TODO 501 save
    }

    public boolean doesDropOnBreak() {
        return dropsOnBreak;
    }

    public void setDropOnBreak(boolean dropsOnBreak) {
        this.dropsOnBreak = dropsOnBreak;
        //TODO 501 save
    }


}
