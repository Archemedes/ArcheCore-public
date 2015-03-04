package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.SQL.*;
import net.lordofthecraft.arche.*;

public abstract class ArcheTask implements Runnable
{
    protected static SQLHandler handle;
    
    protected ArcheTask() {
        super();
        if (ArcheTask.handle == null) {
            ArcheTask.handle = ArcheCore.getPlugin().getSQLHandler();
        }
    }
    
    @Override
    public abstract void run();
    
    static {
        ArcheTask.handle = null;
    }
}
