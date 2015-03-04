package net.lordofthecraft.arche.save;

import net.lordofthecraft.arche.save.tasks.*;
import java.util.concurrent.*;

public class SaveHandler
{
    private BlockingDeque<ArcheTask> saveQueue;
    
    public SaveHandler() {
        super();
        this.saveQueue = new LinkedBlockingDeque<ArcheTask>();
    }
    
    public boolean isEmpty() {
        return this.saveQueue.isEmpty();
    }
    
    public void put(final ArcheTask s) {
        try {
            this.saveQueue.add(s);
        }
        catch (IllegalStateException e) {
            throw new IllegalStateException("ArcheCore save buffer is full! Task rejected.", e);
        }
    }
    
    public ArcheTask take() {
        try {
            return this.saveQueue.take();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public ArcheTask poll() {
        return this.saveQueue.poll();
    }
    
    public static SaveHandler getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    private static class SingletonHolder
    {
        private static final SaveHandler INSTANCE;
        
        static {
            INSTANCE = new SaveHandler();
        }
    }
}
