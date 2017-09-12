package net.lordofthecraft.arche.save.archerows;

import java.sql.Connection;
import java.sql.SQLException;

public interface ArchePreparedStatementRow extends ArcheRow {

    void setConnection(Connection connection);

    void executeStatements() throws SQLException;
}
