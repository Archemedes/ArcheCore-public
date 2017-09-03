package net.lordofthecraft.arche.save.tasks;

/**
 * Created on 9/2/2017
 *
 * @author 501warhead
 */
public class BeginTransactionTask extends ArcheTask {

    public BeginTransactionTask() {
    }

    @Override
    public void run() {
        handle.execute("BEGIN TRANSACTION");
    }
}
