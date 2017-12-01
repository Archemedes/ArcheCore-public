package net.lordofthecraft.arche.SQL;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.Closeable;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Represents an SQL connection pool
 *
 * @author diddiz
 * @author 501warhead
 */
public class ConnectionPool implements Closeable {

    private final HikariDataSource ds;

    public static ConnectionPool makeMysqlConnectionPool(String url, String user, String password, int timeout) throws ClassNotFoundException {
        return new ConnectionPool(url, user, password, timeout);
    }

    public static ConnectionPool makeSQLiteConnectionPool(File file, int timeout) throws ClassNotFoundException {
        return new ConnectionPool(file, timeout);
    }

    public ConnectionPool(String url, String user, String password, int timeout) throws ClassNotFoundException {
        this.ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(user);
        ds.setPassword(password);

        ds.setPoolName("ArcheCore-Connection-Pool");

        ds.setMinimumIdle(2);
        ds.setIdleTimeout(timeout);
        ds.setMaxLifetime(timeout);
        ds.setLeakDetectionThreshold(100000);
        ds.addDataSourceProperty("useUnicode", "true");
        ds.addDataSourceProperty("characterEncoding", "utf-8");
        ds.addDataSourceProperty("rewriteBatchedStatements", "true");
        ds.addDataSourceProperty("useServerPrepStmts", "true");

        ds.addDataSourceProperty("cachePrepStmts", "true");
        ds.addDataSourceProperty("prepStmtCacheSize", "250");
        ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

    }

    private ConnectionPool(File file, int timeout) throws ClassNotFoundException {
        HikariConfig config = new HikariConfig();
        config.setPoolName("ArcheCore-Connection-Pool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
        config.setMaximumPoolSize(10);
        config.setIdleTimeout(timeout);
        config.setMaxLifetime(timeout);
        config.setLeakDetectionThreshold(100000);
        ds = new HikariDataSource(config);
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
