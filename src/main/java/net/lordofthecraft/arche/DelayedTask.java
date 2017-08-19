package net.lordofthecraft.arche;

/**
 * Created by teegah on 2/14/2017.
 */

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public abstract class DelayedTask {
    private Plugin plugin;

    protected Plugin plugin() {
        if (plugin == null) {
            plugin = ArcheCore.getPlugin();
        }
        return plugin;
    }

    public DelayedTask(int delay) {
        this.delay = delay;
    }



    protected int delay;
    protected int taskId;
    protected boolean started;

    protected DelayedTask object() {
        return this;
    }

    public void start() {
        started = true;
        taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin(), () -> {
            started = false;
            object().run();
        }, delay);
    }


    public abstract void run();

    public void stop() {
        if (started) {
            try {
                started = false;
                Bukkit.getScheduler().cancelTask(taskId);
            }catch(Exception ignored) {  }
        }
    }

    public boolean isRunning() {
        return started;
    }
}
