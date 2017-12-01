package net.lordofthecraft.arche.SQL;

import net.lordofthecraft.arche.ArcheCore;
import org.bukkit.plugin.Plugin;

import javax.sql.DataSource;
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
			plugin.getLogger().severe(e.getMessage());
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
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "Error occurred while saving "+name+"_"+time+".db!", e);
            }
        } else {
            ArcheCore.getPlugin().getLogger().severe(name+".db doesn't exist?");
        }
        //Files.copy(path+name+".db", path+name+format.format(date)+".db", (CopyOption) StandardCopyOption.COPY_ATTRIBUTES);
    }

	/*@Override
	public void createTable(String table, Map<String, String> criteria){
		StringBuilder buffer = new StringBuilder(64);
		String pretext = "CREATE TABLE IF NOT EXISTS " + table + " ";
		
		String div = "";
		for(Entry<String, String> entry : criteria.entrySet()){
			buffer.append(div);
			div = ", ";
			buffer.append(entry.getKey()).append(" ");
			buffer.append(entry.getValue().toUpperCase());
		}
		
		//System.out.println(pretext + "(" + buffer.toString() + ");");
		execute(pretext + "(" + buffer.toString() + ");");
	}*/

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

	/*@Override
	public synchronized Map<String, LinkedList<Object>> select(String table, List<String> columns, Map<String, Object> criteria){
		Map<String, LinkedList<Object>> result = Maps.newHashMap();
		ResultSet res = null;
	
		String cols;
		if(columns == null){ 
			cols = "*";
		}else{
			StringBuilder buffer = new StringBuilder(64);
			String div = "";
			for(String s : columns){
				buffer.append(div);
				div = ",";
				buffer.append(s);
			}
			cols = buffer.toString();
		}
		
		String pretext = "SELECT " + cols + " FROM " + table;
		String query = pretext + SQLUtils.giveOptionalWhere(criteria);
		try{
			res = sqlite.query(query);
			
			ResultSetMetaData rsmd = res.getMetaData();
			int count = rsmd.getColumnCount();
			LinkedList<LinkedList<Object>> data = new LinkedList<LinkedList<Object>>();
			
			for(int i = 1; i <= count; i++){
				String label = rsmd.getColumnLabel(i);
				LinkedList<Object> vals = Lists.newLinkedList();
				data.add(vals);
				result.put(label, vals);
			}
			
			while(res.next()){
				for(int i = 1; i <= count; i++){
					data.get(i-1).add(res.getObject(i));
				}
			}
		}catch(SQLException e){e.printStackTrace();}
		finally{SQLUtils.closeStatement(res);}
		return result;
	}*/

	/*@Override
	public void update(String table, Map<String, Object> values, Map<String, Object> criteria){
		if(values == null) throw new NullPointerException("Columns to set cannot be null");
		
		String pretext = "UPDATE " + table + " SET ";
		String vals = SQLUtils.formatSetClause(values);
		
		String query = pretext + vals + SQLUtils.giveOptionalWhere(criteria);
		execute(query);
	}*/

	/*@Override
	public int insert(String table, Map<String, Object> values){
		ResultSet res = null;
		StringBuilder cols = new StringBuilder().append('(');
		StringBuilder vals = new StringBuilder().append('(');
		String prefix = "";
		
		//Create a query from the map of keys and values. Keys are the column IDs.
		for(Entry<String,Object> e : values.entrySet()){
			Object o = e.getValue();
			cols.append(prefix);
			vals.append(prefix);
			prefix = ",";
			cols.append(e.getKey());
			if( o == null) o = new Syntax("NULL");
			else if(o instanceof Boolean) o = ((Boolean) o)? 1:0;
			else if(o instanceof String) o = ((String) o).replace(';', ' ').replace("'", "''");
			boolean noQuotes = (o instanceof Number) || (o instanceof Syntax);
			vals.append(noQuotes? o.toString() : "'" + o.toString() + "'");
		}
		
		cols.append(')');
		vals.append(')');
		
		int result = -1;
		//State the Query
		String query = "INSERT INTO " + table + " " + cols.toString() + " VALUES " + vals.toString() + ";";
		try{ //SQL Stuff
			synchronized(this){
				query(query);
				res = sqlite.query("SELECT last_insert_rowid() FROM " + table);
				if(res.next()) result = res.getInt("last_insert_rowid()");
				SQLUtils.closeStatement(res);
			}
		} catch(SQLException se){se.printStackTrace();}
		finally{SQLUtils.closeStatement(res);}
		
		return result;
	}*/

	@Override
	public void remove(String table, Map<String, Object> criteria){
		String pretext = "DELETE FROM " + table;
		String query = pretext + SQLUtils.giveOptionalWhere(criteria);

		execute(query);
	}

	@Override
	public synchronized void execute(String query){
		ResultSet res = null;

		try {
			res = sqlite.query(query);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			SQLUtils.closeStatement(res);
		}
	}

	@Override
	public Connection getConnection() {
        try {
            return sqlite.getConnection();
        } catch (SQLException e) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "Failed to create a Connection object inside of SQLiteHandler!: ", e);
            return null;
        }
    }
}
