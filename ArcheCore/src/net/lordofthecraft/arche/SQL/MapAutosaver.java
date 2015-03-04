package net.lordofthecraft.arche.SQL;

import java.util.*;
import org.bukkit.plugin.*;
import com.google.common.collect.*;
import java.io.*;
import java.sql.*;

public class MapAutosaver
{
    private final SQLite sql;
    private final Connection con;
    private final Set<String> names;
    
    public MapAutosaver(final Plugin plugin) {
        super();
        this.names = Sets.newHashSet();
        this.sql = new SQLite(plugin.getLogger(), plugin.getName(), plugin.getDataFolder().getAbsolutePath(), "arche_autosave");
        this.con = this.sql.getConnection();
    }
    
    public <T extends Serializable> AutosavedHashMap<T> newAutosavedHashMap(String name, final boolean reflective) {
        name = name.toLowerCase();
        if (this.names.contains(name)) {
            throw new IllegalArgumentException("Table name '" + name + "' already in use!");
        }
        final AutosavedHashMap<T> result = new AutosavedHashMap<T>(this.con, name, reflective);
        try {
            this.sql.query("CREATE TABLE IF NOT EXISTS " + name + " (key TEXT UNIQUE REPLACE, value BLOB);");
            if (reflective) {
                ObjectInputStream objectIn = null;
                final Statement state = this.con.createStatement();
                final ResultSet res = state.executeQuery("SELECT * FROM " + name + ";");
                while (res.next()) {
                    final byte[] buf = res.getBytes(2);
                    if (buf != null) {
                        objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
                        final String key = res.getString(1);
                        final T value = (T)objectIn.readObject();
                        objectIn.close();
                        result.put(key, value);
                    }
                }
                res.close();
                state.close();
                return result;
            }
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
