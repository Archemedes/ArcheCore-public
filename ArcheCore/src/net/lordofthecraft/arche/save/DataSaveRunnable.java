package net.lordofthecraft.arche.save;

import net.lordofthecraft.arche.*;
import net.lordofthecraft.arche.SQL.*;
import org.bukkit.*;
import net.lordofthecraft.arche.save.tasks.*;

public class DataSaveRunnable implements Runnable
{
    private final SaveHandler queue;
    private final ArcheTimer timer;
    private final SQLHandler handle;
    
    public DataSaveRunnable(final SaveHandler queue, final ArcheTimer timer, final SQLHandler handle) {
        super();
        this.queue = queue;
        this.timer = timer;
        this.handle = handle;
    }
    
    @Override
    public void run() {
        while (true) {
            try {
            Block_4:
                while (true) {
                    ArcheTask task = this.queue.take();
                    int i = 0;
                    this.handle.execute("BEGIN");
                    if (this.timer != null) {
                        Bukkit.getLogger().info("[ArcheCore][Debug] Starting an ArcheCore SaveHandler transaction now.");
                    }
                    do {
                        if (this.timer != null) {
                            this.timer.startTiming(task.getClass().getSimpleName());
                        }
                        task.run();
                        if (this.timer != null) {
                            this.timer.stopTiming(task.getClass().getSimpleName());
                        }
                        if (task instanceof EndOfStreamTask) {
                            break Block_4;
                        }
                        if (i++ >= 1000) {
                            task = null;
                        }
                        else {
                            task = this.queue.poll();
                        }
                    } while (task != null);
                    this.handle.execute("END TRANSACTION");
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                this.handle.execute("END TRANSACTION");
                continue;
            }
            break;
        }
    }
}
