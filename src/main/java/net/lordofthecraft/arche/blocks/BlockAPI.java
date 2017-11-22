package net.lordofthecraft.arche.blocks;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.save.Consumer;
import net.lordofthecraft.arche.util.WeakBlock;

import java.util.Map;

public final class BlockAPI {

    private final Map<WeakBlock, BlockData> blocks = Maps.newConcurrentMap();
    private final Consumer consumer;

    public BlockAPI(Consumer consumer) {
        this.consumer = consumer;
    }

    public void init(SQLHandler handler) {
        if (!blocks.isEmpty()) {
            return;
        }
    }

    public BlockData getData(WeakBlock wb) {
        return blocks.get(wb);
    }

    public void registerBlockData(BlockData data) {
        if (blocks.containsKey(data.block)) {
            blocks.remove(data.block);
            //TODO 501 Remove
        }
        blocks.put(data.block, data);
        //TODO 501 save
    }
}
