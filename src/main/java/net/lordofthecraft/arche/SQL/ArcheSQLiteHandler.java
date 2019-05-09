package net.lordofthecraft.arche.SQL;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;

import javax.sql.DataSource;

import org.bukkit.plugin.Plugin;

import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.util.SQLUtil;

public class ArcheSQLiteHandler extends SQLHandler {
	private final SQLite sqlite;
	
	/**
	 * Create a SQLite database for your Plugin, and creating this object for interaction with said database.
	 * @param plugin The Plugin to create this database for; this affects the db file's location
	 * @param identifier The (file)name of the database.
	 */
    public ArcheSQLiteHandler(Plugin plugin, String identifier, int timeout) {
    	sqlite = new SQLite(plugin.getLogger(), identifier, plugin.getDataFolder().getAbsolutePath(), identifier, timeout);
    	try
    	{
    		sqlite.open();
    	}
    	catch (Exception e)
    	{
    		CoreLog.log(Level.SEVERE, e.getMessage(), e);
    	}
    }

    @Override
    public ConnectionPool getPool() {
        return sqlite.pool;
    }

    @Override
    public DataSource getDataSource() {
		return sqlite.getDataSource();
	}

	@Override
	public SQLite getSQL(){
		return sqlite;
	}

	@Override
	public synchronized final void close() {
		sqlite.close();
	}

	@Override
	public synchronized ResultSet query(String sql) throws SQLException{
		return sqlite.query(sql);
	}

	@Override
	public void remove(String table, Map<String, Object> criteria){
		String pretext = "DELETE FROM " + table;
		String query = pretext + SQLUtil.giveOptionalWhere(criteria);

		execute(query);
	}

	@Override
	public void execute(String query){
		ResultSet res = null;

		try {
			res = sqlite.query(query);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SQLUtil.closeStatement(res);
		}
	}

	@Override
	public Connection getConnection() {
        try {
            return sqlite.getConnection();
        } catch (SQLException e) {
            CoreLog.log(Level.SEVERE, "Failed to create a Connection object inside of SQLiteHandler!: ", e);
            return null;
        }
    }
}
