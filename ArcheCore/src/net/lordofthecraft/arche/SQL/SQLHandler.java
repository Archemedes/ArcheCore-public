package net.lordofthecraft.arche.SQL;

import org.bukkit.plugin.*;
import java.util.*;
import com.google.common.collect.*;
import java.sql.*;

public class SQLHandler
{
    private final SQLite sqlite;
    
    public SQLHandler(final Plugin plugin, final String identifier) {
        super();
        this.sqlite = new SQLite(plugin.getLogger(), identifier, plugin.getDataFolder().getAbsolutePath(), identifier);
        try {
            this.sqlite.open();
        }
        catch (Exception e) {
            plugin.getLogger().severe(e.getMessage());
        }
    }
    
    public void createTable(final String table, final Map<String, String> criteria) {
        final StringBuilder buffer = new StringBuilder(64);
        final String pretext = "CREATE TABLE IF NOT EXISTS " + table + " ";
        String div = "";
        for (final Map.Entry<String, String> entry : criteria.entrySet()) {
            buffer.append(div);
            div = ", ";
            buffer.append(entry.getKey() + " ");
            buffer.append(entry.getValue().toUpperCase());
        }
        this.execute(pretext + "(" + buffer.toString() + ");");
    }
    
    public SQLite getSQL() {
        return this.sqlite;
    }
    
    public final synchronized boolean close() {
        return this.sqlite.close();
    }
    
    private void closeStatement(final ResultSet res) {
        if (res != null) {
            try {
                res.close();
                res.getStatement().close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    public synchronized ResultSet query(final String sql) throws SQLException {
        return this.sqlite.query(sql);
    }
    
    public synchronized Map<String, LinkedList<Object>> select(final String table, final List<String> columns, final Map<String, Object> criteria) {
        final Map<String, LinkedList<Object>> result = Maps.newHashMap();
        ResultSet res = null;
        String cols;
        if (columns == null) {
            cols = "*";
        }
        else {
            final StringBuilder buffer = new StringBuilder(64);
            String div = "";
            for (final String s : columns) {
                buffer.append(div);
                div = ",";
                buffer.append(s);
            }
            cols = buffer.toString();
        }
        final String pretext = "SELECT " + cols + " FROM " + table;
        final String query = pretext + this.giveOptionalWhere(criteria);
        try {
            res = this.sqlite.query(query);
            final ResultSetMetaData rsmd = res.getMetaData();
            final int count = rsmd.getColumnCount();
            final LinkedList<LinkedList<Object>> data = new LinkedList<LinkedList<Object>>();
            for (int i = 1; i <= count; ++i) {
                final String label = rsmd.getColumnLabel(i);
                final LinkedList<Object> vals = (LinkedList<Object>)Lists.newLinkedList();
                data.add(vals);
                result.put(label, vals);
            }
            while (res.next()) {
                for (int i = 1; i <= count; ++i) {
                    data.get(i - 1).add(res.getObject(i));
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            this.closeStatement(res);
        }
        return result;
    }
    
    public void update(final String table, final Map<String, Object> values, final Map<String, Object> criteria) {
        if (values == null) {
            throw new NullPointerException("Columns to set cannot be null");
        }
        final String pretext = "UPDATE " + table + " SET ";
        final String vals = this.formatSetClause(values);
        final String query = pretext + vals + this.giveOptionalWhere(criteria);
        this.execute(query);
    }
    
    public int insert(final String table, final Map<String, Object> values) {
        ResultSet res = null;
        final StringBuilder cols = new StringBuilder().append('(');
        final StringBuilder vals = new StringBuilder().append('(');
        String prefix = "";
        for (final Map.Entry<String, Object> e : values.entrySet()) {
            Object o = e.getValue();
            cols.append(prefix);
            vals.append(prefix);
            prefix = ",";
            cols.append(e.getKey());
            if (o == null) {
                o = new Syntax("NULL");
            }
            else if (o instanceof Boolean) {
                o = (((boolean)o) ? 1 : 0);
            }
            else if (o instanceof String) {
                o = ((String)o).replace(';', ' ').replace("'", "''");
            }
            final boolean noQuotes = o instanceof Number || o instanceof Syntax;
            vals.append(noQuotes ? o.toString() : ("'" + o.toString() + "'"));
        }
        cols.append(')');
        vals.append(')');
        int result = -1;
        final String query = "INSERT INTO " + table + " " + cols.toString() + " VALUES " + vals.toString() + ";";
        try {
            synchronized (this) {
                this.query(query);
                res = this.sqlite.query("SELECT last_insert_rowid() FROM " + table);
                if (res.next()) {
                    result = res.getInt("last_insert_rowid()");
                }
                this.closeStatement(res);
            }
        }
        catch (SQLException se) {
            se.printStackTrace();
        }
        finally {
            this.closeStatement(res);
        }
        return result;
    }
    
    public void remove(final String table, final Map<String, Object> criteria) {
        final String pretext = "DELETE FROM " + table;
        final String query = pretext + this.giveOptionalWhere(criteria);
        this.execute(query);
    }
    
    public synchronized void execute(final String query) {
        ResultSet res = null;
        try {
            res = this.sqlite.query(query);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            this.closeStatement(res);
        }
    }
    
    private String giveOptionalWhere(final Map<String, Object> criteria) {
        return (criteria == null) ? "" : (" WHERE " + this.formatWhereClause(criteria));
    }
    
    private String formatWhereClause(final Map<String, Object> val) {
        String div = "";
        final StringBuilder result = new StringBuilder();
        for (final Map.Entry<String, Object> entry : val.entrySet()) {
            result.append(div);
            div = " AND ";
            result.append(entry.getKey() + '=');
            Object o = entry.getValue();
            if (o == null) {
                o = new Syntax("NULL");
            }
            else if (o instanceof Boolean) {
                o = (((boolean)o) ? 1 : 0);
            }
            else if (o instanceof String) {
                o = ((String)o).replace(';', ' ').replace("'", "''");
            }
            final boolean noQuotes = o instanceof Number || o instanceof Syntax;
            final String condition = noQuotes ? o.toString() : ("'" + o.toString() + "'");
            result.append(condition);
        }
        return result.toString();
    }
    
    private String formatSetClause(final Map<String, Object> val) {
        String div = "";
        final StringBuilder result = new StringBuilder();
        for (final Map.Entry<String, Object> entry : val.entrySet()) {
            result.append(div);
            div = ",";
            result.append(entry.getKey() + '=');
            Object o = entry.getValue();
            if (o == null) {
                o = new Syntax("NULL");
            }
            else if (o instanceof Boolean) {
                o = (((boolean)o) ? 1 : 0);
            }
            else if (o instanceof String) {
                o = ((String)o).replace('(', ' ').replace(')', ' ').replace(';', ' ').replace("'", "''");
            }
            final boolean noQuotes = o instanceof Number || o instanceof Syntax;
            final String condition = noQuotes ? o.toString() : ("'" + o.toString() + "'");
            result.append(condition);
        }
        return result.toString();
    }
}
