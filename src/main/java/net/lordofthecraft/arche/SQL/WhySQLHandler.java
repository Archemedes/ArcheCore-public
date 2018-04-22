package net.lordofthecraft.arche.SQL;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Level;

import javax.sql.DataSource;

import org.bukkit.configuration.ConfigurationSection;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.util.SQLUtil;

/**
 * Represents an SQLHandler affiliated with MySQL
 *
 * @author 501warhead (Why)
 */
public class WhySQLHandler extends SQLHandler {

    private final ConnectionPool pool;

    public WhySQLHandler(String user, String password, int timeout) throws ClassNotFoundException {
        pool = ConnectionPool.makeMysqlConnectionPool(getUrl(), user, password, timeout);
    }

    @Override
    public ConnectionPool getPool() {
        return pool;
    }

    public static String getUrl() {
        final ConfigurationSection config = ArcheCore.getPlugin().getConfig();
        return "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getInt("mysql.port") + "/" + getStringIncludingInts(config, "mysql.database");
    }

    private static String getStringIncludingInts(ConfigurationSection cfg, String key) {
        String str = cfg.getString(key);
        if (str == null) {
            str = String.valueOf(cfg.getInt(key));
        }
        if (str == null) {
            str = "No value set for '" + key + "'";
        }
        return str;
    }

    @Override
    public Closeable getSQL() {
        return pool;
    }

    @Override
    public void close() {
        pool.close();
    }

    @Override
    public ResultSet query(String sql) throws SQLException {
        Connection c = pool.getConnection();
        Statement statement = c.createStatement();
        if (statement.execute(sql)) {
            return statement.getResultSet();
        } else {
            statement.close();
            c.close();
            return null;
        }
    }

    @Override
    public DataSource getDataSource() {
        return pool.getDatasource();
    }

    @Override
    public void remove(String table, Map<String, Object> criteria) {
        String pretext = "DELETE FROM " + table;
        String query = pretext + SQLUtil.giveOptionalWhere(criteria);

        execute(query);
    }

    @Override
    public void execute(String query) {
        ResultSet res = null;
        try {
            res = query(query);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtil.closeStatement(res);
        }
    }

    @Override
    public Connection getConnection() {
        try {
            return pool.getConnection();
        } catch (SQLException e) {
            CoreLog.log(Level.SEVERE, "Failed to get the MySQL connection!", e);
            return null;
        }
    }
}
