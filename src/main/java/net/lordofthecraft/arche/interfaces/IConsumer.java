package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.save.rows.ArcheRow;

public interface IConsumer {

    /**
     * Adds a new row to the Consumer to process for SQL. This is for SQL Update/Insert/Delete/Drop tasks only.
     * This is not to be used for Queries.
     *
     * @param row The ArcheRow to be queued.
     */
    void queueRow(ArcheRow row);

    int getQueueSize();
}
