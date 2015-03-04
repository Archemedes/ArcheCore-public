package net.lordofthecraft.arche.save.tasks;

public final class EndOfStreamTask extends ArcheTask
{
    @Override
    public void run() {
        EndOfStreamTask.handle.execute("END TRANSACTION");
    }
}
