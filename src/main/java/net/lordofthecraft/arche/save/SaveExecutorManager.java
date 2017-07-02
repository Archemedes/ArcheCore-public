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

    private final ExecutorService SAVESERVICE;

    public SaveExecutorManager() {
        SAVESERVICE = Executors.newCachedThreadPool();
    }

    public void submit(ArcheTask t) {
        if (SAVESERVICE.isShutdown()) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "ArcheCore caught a task being submitted after the save executor service has been shutdown. Type: "+t.toString());
            return;
        }
        SAVESERVICE.submit(t);
        if (t instanceof EndOfStreamTask) {
            closeExecutor();
        }
    }

    public Future<?> call(Callable<?> call) {
        if (SAVESERVICE.isShutdown()) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "ArcheCore caught a task being submitted after the save executor service has been shutdown. Type: "+call.toString());
            return null;
        }
        return SAVESERVICE.submit(call);
    }

    protected void closeExecutor() {

    }

    private void a() {
    }
}
