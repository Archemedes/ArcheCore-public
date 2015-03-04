package net.lordofthecraft.arche.SQL;

import java.util.*;
import java.io.*;
import java.sql.*;

public class AutosavedHashMap<V extends Serializable> extends HashMap<String, V>
{
    private static final long serialVersionUID = 8033169651578143510L;
    private final String name;
    private final Connection con;
    private final boolean reflective;
    
    AutosavedHashMap(final Connection con, final String name, final boolean reflective) {
        super();
        this.con = con;
        this.name = name;
        this.reflective = reflective;
    }
    
    public V loadValue(final String key) {
        if (this.reflective) {
            throw new UnsupportedOperationException("Can't load for reflective maps.");
        }
        ObjectInputStream objectIn = null;
        try {
            final Statement state = this.con.createStatement();
            V result = null;
            final ResultSet res = state.executeQuery("SELECT value FROM " + this.name + "WHERE key='" + key + "';");
            if (res.next()) {
                final byte[] buf = res.getBytes(1);
                if (buf == null) {
                    throw new SQLException();
                }
                objectIn = new ObjectInputStream(new ByteArrayInputStream(buf));
                result = (V)objectIn.readObject();
                super.put(key, result);
            }
            res.close();
            state.close();
            if (objectIn != null) {
                objectIn.close();
            }
            return result;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public V put(final String key, final V value) {
        try {
            final PreparedStatement pstm = this.con.prepareStatement("INSERT INTO " + this.name + "(key, value) VALUES (?, ?)");
            pstm.setString(1, key);
            pstm.setObject(2, value);
            pstm.executeUpdate();
            pstm.close();
            return super.put(key, value);
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public V remove(final Object o) {
        if (!this.reflective) {
            return super.remove(o);
        }
        if (!(o instanceof String)) {
            return null;
        }
        final String key = (String)o;
        return this.removeFromTable(key);
    }
    
    public V removeFromTable(final String key) {
        try {
            final Statement state = this.con.createStatement();
            state.executeQuery("DELETE FROM " + this.name + "WHERE key='" + key + "';");
            state.close();
            return super.remove(key);
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public void clear() {
        if (this.reflective) {
            this.clearTable();
        }
        else {
            super.clear();
        }
    }
    
    public void clearTable() {
        try {
            final Statement state = this.con.createStatement();
            state.executeQuery("DELETE FROM " + this.name + ";");
            state.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        super.clear();
    }
}
