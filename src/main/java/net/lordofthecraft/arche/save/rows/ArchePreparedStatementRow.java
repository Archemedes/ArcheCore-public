package net.lordofthecraft.arche.save.rows;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents a "task" which runs {@link java.sql.PreparedStatement}s when given a {@link Connection}.
 * <p>
 * Statements are not initially run when executed and are instead added to a {@link Connection} which has {@link Connection#setAutoCommit(boolean)} set to {@code 'FALSE'}
 * All statements are committed through {@link Connection#commit()} at once at the end of the processing all tasks in {@link net.lordofthecraft.arche.save.Consumer}
 *
 * @author 501warhead
 */
public interface ArchePreparedStatementRow extends ArcheRow {

    /**
     * Sets the connection of this row. All ArchePerparedStatementRows should save this connection and use it for all SQL
     *
     * <b>Do <i>not</i> close this connection.</b>
     * <b>Do not change settings on this connection</b>
     *
     * <b>No really, DO NOT CLOSE THIS CONNECTION.</b>
     * @param connection The connection to use
     */
    void setConnection(Connection connection);

    /**
     * This is the method in which PreparedStatements should be created, binded, executed, and closed.
     * @throws SQLException Thrown if anything errors.
     */
    void executeStatements() throws SQLException;
}
