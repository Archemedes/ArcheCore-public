package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.save.rows.ArcheRow;
import net.lordofthecraft.arche.save.rows.FlexibleRow;

public interface IConsumer {

    /**
     * Adds a new row to the Consumer to process for SQL. This is for SQL Update/Insert/Delete/Drop tasks only.
     * This is not to be used for Queries.
     *
     * @param row The ArcheRow to be queued.
     */
    void queueRow(ArcheRow row);
    
    /**
     * @return If config is set to debug to consumer
     */
    boolean isDebugging();

    int getQueueSize();
    
    public FlexibleRow insert(String table);
    public FlexibleRow insertIgnore(String table);
    public FlexibleRow replace(String table);
    public FlexibleRow delete(String table);
}
