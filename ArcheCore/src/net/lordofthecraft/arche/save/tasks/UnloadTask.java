package net.lordofthecraft.arche.save.tasks;

import java.util.*;
import org.bukkit.entity.*;
import org.bukkit.plugin.*;
import net.lordofthecraft.arche.*;
import org.bukkit.*;
import net.lordofthecraft.arche.event.*;
import org.bukkit.event.*;
import net.lordofthecraft.arche.persona.*;

public class UnloadTask extends ArcheTask
{
    private final String name;
    private final UUID uuid;
    
    public UnloadTask(final Player who) {
        super();
        this.name = who.getName();
        this.uuid = who.getUniqueId();
    }
    
    @Override
    public void run() {
        final Plugin plugin = (Plugin)ArcheCore.getControls();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, (Runnable)new UnloadRunnable());
        Bukkit.getPluginManager().callEvent((Event)new AsyncPlayerUnloadEvent(this.name, this.uuid));
    }
    
    private class UnloadRunnable implements Runnable
    {
        @Override
        public void run() {
            final ArchePersonaHandler h = (ArchePersonaHandler)ArcheCore.getControls().getPersonaHandler();
            h.unload(UnloadTask.this.uuid);
        }
    }
}
