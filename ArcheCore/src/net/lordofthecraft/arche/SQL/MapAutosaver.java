package net.lordofthecraft.arche.SQL;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import org.bukkit.plugin.Plugin;

import com.google.common.collect.Sets;

public class MapAutosaver {
	
	private final SQLite sql;
	private final Connection con;
	private final Set<String> names = Sets.newHashSet();
	
	public MapAutosaver(Plugin plugin){
		sql = new SQLite(plugin.getLogger(), plugin.getName(), plugin.getDataFolder().getAbsolutePath(), "arche_autosave");
		con = sql.getConnection();
		
	}
	
	public <T extends Serializable> AutosavedHashMap<T> newAutosavedHashMap(String name, boolean reflective){
		name = name.toLowerCase();
		if(names.contains(name)) throw new IllegalArgumentException("Table name '" + name + "' already in use!");
		
		AutosavedHashMap<T> result = new AutosavedHashMap<T>(con, name, reflective);
		try {
			sql.query("CREATE TABLE IF NOT EXISTS " + name + " (key TEXT UNIQUE REPLACE, value BLOB);");
			if(reflective){
				ObjectInputStream objectIn = null;
				
				Statement state = con.createStatement();
				
				ResultSet res = state.executeQuery("SELECT * FROM " + name + ";");
				while(res.next()){
				    byte[] buf = res.getBytes(2);
				    
				    if (buf != null){
				      objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
				      String key = res.getString(1);
				      @SuppressWarnings("unchecked")
				      T value = (T) objectIn.readObject();
				      objectIn.close();
				      result.put(key, value);
				    }
					
				};
			
				res.close();
				state.close();
				return result;
			}
			return result;
		}catch(Exception e){e.printStackTrace();}
		return null;
		
	}
	
}

