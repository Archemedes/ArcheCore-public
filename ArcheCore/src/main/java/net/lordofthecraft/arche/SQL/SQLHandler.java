package net.lordofthecraft.arche.SQL;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

public class SQLHandler {
	private final SQLite sqlite;
    private final Plugin plugin;
    private final String name;
	
	/**
	 * Create a SQLite database for your Plugin, and creating this object for interaction with said database.
	 * @param plugin The Plugin to create this database for; this affects the db file's location
	 * @param identifier The (file)name of the database.
	 */
	public SQLHandler(Plugin plugin, String identifier){
		sqlite = new SQLite(plugin.getLogger(), identifier, plugin.getDataFolder().getAbsolutePath(), identifier);
        this.plugin = plugin;
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

    /**
     * Clones the database for this SQLHandler.
     * Use with EXTREME caution. Will lock the DB for the duration of the clone, so don't use during runtime if you're
     * constantly using the SQLite connection
     */

    public void cloneDB() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH");
        String time = format.format(new Date());
        File file = new File(plugin.getDataFolder(), name+".db");
        if (file.exists()) {
            Path path = file.getAbsoluteFile().toPath();
            File dir = new File(plugin.getDataFolder(), name+"_logs");
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
	
	/**
	 * Create a table within the database if it doesn't already exists.
	 * @param table The name of the table
	 * @param criteria A Map containing the column names as the key, and the criteria (Type, defaults, etc.) as the values.
	 */
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
	}
	
	/**
	 * Retrieves the underlying SQLite object that handles database queries and statement creations.
	 * You should need to call this method for formatted SQLite queries. Use {@link #query(String)} instead.
	 * @return The SQLite object.
	 */
	public SQLite getSQL(){
		return sqlite;
	}
	
	/**
	 * Close the database connection.
	 * @return Whether the database was successfully closed.
	 */
	public synchronized final boolean close(){
		return sqlite.close();
	}
	
	/**
	 * Close the given ResultSet and the Statement that created the ResultSet
	 * @param res the ResultSet object to close.
	 */
	private void closeStatement(ResultSet res){
		if(res != null){
			try {
				res.close();
				res.getStatement().close();
			} catch (SQLException e) {e.printStackTrace();}
		}
	}
	
	/**
	 * query the database and returns a ResultSet object
	 * @param sql The SQL syntax to use in the query
	 * @return the ResultSet from the executed statement.
	 * @throws SQLException 
	 */
	public synchronized ResultSet query(String sql) throws SQLException{
		return sqlite.query(sql);
	}
	
	/**
	 * Execute a select statement and sort the result into a Map.
	 * Usage is discouraged. Perform a direct query instead for higher efficiency and control.
	 * @param table The Table to select from.
	 * @param columns The names of the columns you wish to select.
	 * @param criteria The SQLite criteria, with the keys being the column names and the values to be matched via 'equals' (=)
	 * @return the matched rows from the SQL table, with the column name as the key.
	 */
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
		String query = pretext + giveOptionalWhere(criteria);
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
		finally{closeStatement(res);}	
		return result;
	}
	
	/**
	 * Update one or more rows in the database
	 * @param table The Table to update
	 * @param values The values to update, the keys of the maps being the column names.
	 * @param criteria The criteria to match, the keys of the map serving as column names, and the values of the map the values which must match (an equals operation) the database row. 
	 */
	public void update(String table, Map<String, Object> values, Map<String, Object> criteria){
		if(values == null) throw new NullPointerException("Columns to set cannot be null");
		
		String pretext = "UPDATE " + table + " SET ";
		String vals = formatSetClause(values);
		
		String query = pretext + vals + giveOptionalWhere(criteria);
		execute(query);
	}
	
	/**
	 * Insert a new row into a database table
	 * @param table The table to query
	 * @param values The values to insert, the keys of the maps being the column names of the table.
	 * @return the unique rowid of your inserted row
	 */
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
				closeStatement(res);
			}
		} catch(SQLException se){se.printStackTrace();}
		finally{closeStatement(res);}
		
		return result;
	}
	
	/**
	 * Delete all matching rows from a table
	 * @param table The table to query
	 * @param criteria The criteria to meet (with an equals operation) for a row to be removed, the map keys being the column names
	 */
	public void remove(String table, Map<String, Object> criteria){
		String pretext = "DELETE FROM " + table;
		String query = pretext + giveOptionalWhere(criteria);

		execute(query);
	}
	
	/**
	 * Execute a SQLite statement for which no returning ResultSet is required.
	 * @param query The formatted string to create the statement with
	 */
	public synchronized void execute(String query){
		ResultSet res = null;
		
		try{res = sqlite.query(query);}
		catch(SQLException e){e.printStackTrace();}
		finally{closeStatement(res);}
	}
	
	private String giveOptionalWhere(Map<String, Object> criteria){
		return (criteria == null? "" : " WHERE " + formatWhereClause(criteria));
	}
	
	private String formatWhereClause(Map<String, Object> val){
		String div = "";
		StringBuilder result = new StringBuilder();
		for(Entry<String, Object> entry : val.entrySet()){
			result.append(div);
			div = " AND ";
			result.append(entry.getKey() + '=');
			Object o = entry.getValue();
			if( o == null) o = new Syntax("NULL");
			else if(o instanceof Boolean) o = ((Boolean) o)? 1:0;
			else if(o instanceof String) o = ((String) o).replace(';', ' ').replace("'", "''");
			boolean noQuotes = (o instanceof Number) || (o instanceof Syntax);
			String condition = noQuotes? o.toString() : "'" + o.toString() + "'"; 
			result.append(condition);
		}
		
		return result.toString();
	}
	
	private String formatSetClause(Map<String, Object> val){
		String div = "";
		StringBuilder result = new StringBuilder();
		for(Entry<String, Object> entry : val.entrySet()){
			result.append(div);
			div = ",";
			result.append(entry.getKey() + '=');
			Object o = entry.getValue();
			if( o == null) o = new Syntax("NULL");
			else if(o instanceof Boolean) o = ((Boolean) o)? 1:0;
			else if(o instanceof String) o = ((String) o).replace('(', ' ').replace(')', ' ').replace(';', ' ').replace("'", "''");
			boolean noQuotes = (o instanceof Number) || (o instanceof Syntax);
			String condition = noQuotes? o.toString() : "'" + o.toString() + "'"; 
			result.append(condition);
		}
		
		return result.toString();
		
	}
	
}
