package net.lordofthecraft.arche.save;

import java.util.concurrent.ThreadFactory;

/**
 * Created on 9/2/2017
 *
 * @author 501warhead
 */
public class ArcheThreadFactory implements ThreadFactory {

    private static ThreadGroup group;

    public ArcheThreadFactory() {
        if (group == null) {
            group = new ThreadGroup("Archecore Threads");
        }
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(group, r, "ArcheCore SQL Consumer");
    }

}
