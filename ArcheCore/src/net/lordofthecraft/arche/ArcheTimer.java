package net.lordofthecraft.arche;

import java.util.logging.*;
import org.bukkit.plugin.*;
import com.google.common.collect.*;
import org.apache.commons.lang.*;
import java.util.*;

public class ArcheTimer
{
    private final Logger logger;
    private final Map<String, Long> timings;
    
    ArcheTimer(final Plugin plugin) {
        super();
        this.timings = Maps.newConcurrentMap();
        this.logger = plugin.getLogger();
    }
    
    public void startTiming(final String why) {
        final long time = System.nanoTime();
        Validate.notNull((Object)why);
        this.timings.put(why, time);
    }
    
    public void stopTiming(final String why) {
        final long time = System.nanoTime();
        Validate.notNull((Object)why);
        if (this.timings.containsKey(why)) {
            final long took = time - this.timings.get(why);
            this.logger.info("[Debug] operation '" + why + "' took " + took + "ns");
        }
    }
    
    public void stopAllTiming() {
        final long time = System.nanoTime();
        for (final Map.Entry<String, Long> t : this.timings.entrySet()) {
            this.logger.info("[Debug] timed action '" + t.getKey() + "' taking " + (time - t.getValue()) + "ns");
        }
        this.timings.clear();
    }
}
