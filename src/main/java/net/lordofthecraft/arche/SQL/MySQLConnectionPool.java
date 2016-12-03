package net.lordofthecraft.arche.SQL;

import com.zaxxer.hikari.HikariDataSource;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents an SQL connection pool
 *
 * @author diddiz
 * @author 501warhead
 */
public class MySQLConnectionPool implements Closeable {

    private final HikariDataSource ds;

    public MySQLConnectionPool(String url, String user, String password) throws ClassNotFoundException {
        this.ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(user);
        ds.setPassword(password);

        ds.setMinimumIdle(2);
        ds.setPoolName("ArcheCore-Connection-Pool");

        ds.addDataSourceProperty("useUnicode", "true");
        ds.addDataSourceProperty("characterEncoding", "utf-8");
        ds.addDataSourceProperty("rewriteBatchedStatements", "true");

        ds.addDataSourceProperty("cachePrepStmts", "true");
        ds.addDataSourceProperty("prepStmtCacheSize", "250");
        ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    }

    @Override
    public void close() {
        ds.close();
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public HikariDataSource getDatasource() {
        return ds;
    }
}
