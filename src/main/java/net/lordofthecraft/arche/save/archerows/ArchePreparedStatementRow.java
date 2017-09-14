package net.lordofthecraft.arche.save.archerows;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents a "task" which runs PreparedStatements when given a connection.
 * <p>
 * Statements are not initially run when executed and are instead added to a connection which has autocommit to {@code 'FALSE'}
 * All statements are committed at once at the end of the processing.
 *
 * @author 501warhead
 */
public interface ArchePreparedStatementRow extends ArcheRow {

    /**
     * Sets the connection of this row. All ArchePerparedStatementRows should save this connection and use it for all SQL
     *
     * <b>Do not close this connection.</b>
     * <b>Do not change settings on this connection</b>
     * @param connection The connection to use
     */
    void setConnection(Connection connection);

    /**
     * This is the method in which PreparedStatements should be created, binded, executed, and closed.
     * @throws SQLException Thrown if anything errors.
     */
    void executeStatements() throws SQLException;
}
