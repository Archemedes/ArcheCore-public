package net.lordofthecraft.arche.save;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.tasks.ArcheTask;
import net.lordofthecraft.arche.save.tasks.EndOfStreamTask;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 * Created on 5/8/2017
 *
 * @author 501warhead
 */
public class SaveExecutorManager {

    public static SaveExecutorManager getInstance() {
        return SaveExecutorManager.SingletonHolder.INSTANCE;
    }

    private final ExecutorService SAVESERVICE;

    protected SaveExecutorManager() {
        SAVESERVICE = Executors.newCachedThreadPool();
    }

    public void submit(ArcheTask t) {
        if (SAVESERVICE.isShutdown()) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "ArcheCore caught a task being submitted after the save executor service has been shutdown. Type: " + t.toString());
            return;
        }
        SAVESERVICE.submit(t);
        if (t instanceof EndOfStreamTask) {
            closeExecutor();
        }
    }

    public <V> Future<V> call(Callable<V> call) {
        if (SAVESERVICE.isShutdown()) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "ArcheCore caught a task being submitted after the save executor service has been shutdown. Type: " + call.toString());
            return null;
        }
        return SAVESERVICE.submit(call);
    }

    protected void closeExecutor() {

    }

    public ExecutorService getService() {
        return SAVESERVICE;
    }

    private void a() {
    }

    private static class SingletonHolder {
        private static final SaveExecutorManager INSTANCE = new SaveExecutorManager();
    }
}
