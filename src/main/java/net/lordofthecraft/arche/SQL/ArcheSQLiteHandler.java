package net.lordofthecraft.arche.SQL;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

import javax.sql.DataSource;

import org.bukkit.plugin.Plugin;

import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.util.SQLUtil;

public class ArcheSQLiteHandler extends SQLHandler {
	private final SQLite sqlite;
    private final File folder;
    private final String name;
	
	/**
	 * Create a SQLite database for your Plugin, and creating this object for interaction with said database.
	 * @param plugin The Plugin to create this database for; this affects the db file's location
	 * @param identifier The (file)name of the database.
	 */
    public ArcheSQLiteHandler(Plugin plugin, String identifier, int timeout) {
    	sqlite = new SQLite(plugin.getLogger(), identifier, plugin.getDataFolder().getAbsolutePath(), identifier, timeout);
    	this.folder = plugin.getDataFolder();
    	name = identifier;
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

    /**
	 * Clones the database for this ArcheSQLiteHandler.
	 * Use with EXTREME caution. Will lock the DB for the duration of the clone, so don't use during runtime if you're
	 * constantly using the SQLite connection
	 */

    public void cloneDB() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH");
        String time = format.format(new Date());
        File file = new File(folder, name+".db");
        if (file.exists()) {
            Path path = file.getAbsoluteFile().toPath();
            File dir = new File(folder, name+"_logs");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File newFile = new File(dir, name+"_"+time+".db");
            try {
                if (!newFile.exists()) {
                    Files.createFile(newFile.toPath());
                }
                Files.copy(path, newFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                CoreLog.log(Level.SEVERE, "Error occurred while saving "+name+"_"+time+".db!", e);
            }
        } else {
            CoreLog.severe(name+".db doesn't exist?");
        }
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
