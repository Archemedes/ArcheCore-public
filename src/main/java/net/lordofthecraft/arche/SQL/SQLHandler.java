package net.lordofthecraft.arche.SQL;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;

import javax.sql.DataSource;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a object to mediate between plugins and a SQL Database, functioning for MySQL or SQLite
 *
 * @author 501warhead (Why)
 */
public abstract class SQLHandler {

    public SQLHandler() {
    }

    /**
     * Create a table within the database if it doesn't already exists.
     *
     * @param table    The name of the table
     * @param criteria A Map containing the column names as the key, and the criteria (Type, defaults, etc.) as the values.
     */
    public void createTable(String table, Map<String, String> criteria) {
        StringBuilder buffer = new StringBuilder(64);
        String pretext = "CREATE TABLE IF NOT EXISTS " + table + " ";

        String div = "";
        for (Map.Entry<String, String> entry : criteria.entrySet()) {
            buffer.append(div);
            div = ", ";
            buffer.append(entry.getKey()).append(" ");
            //.toUpperCase actually screws this.
            buffer.append(entry.getValue()/*.toUpperCase()*/);
        }
        String createStatement = pretext + "(" + buffer.toString() + ")ENGINE=InnoDB DEFAULT CHARSET=utf8;";
        if (ArcheCore.getPlugin().debugMode()) {
            ArcheCore.getPlugin().getLogger().info("Creating the following table: " + createStatement);
        }
        execute(createStatement);
    }

    public abstract DataSource getDataSource();

    public abstract ConnectionPool getPool();

    /**
     * Retrieves the underlying SQLite/ConnectionPool object that handles database queries and statement creations.
     * You should need to call this method for formatted SQLite queries. Use {@link #query(String)} instead.
     *
     * @return The SQLite/ConnectionPool object.
     */
    public abstract Closeable getSQL();

    /**
     * Close the database connection.
     */
    public abstract void close();

    /**
     * query the database and returns a ResultSet object
     *
     * @param sql The SQL syntax to use in the query
     * @return the ResultSet from the executed statement.
     * @throws SQLException if the query throws an exception
     */
    public abstract ResultSet query(String sql) throws SQLException;

    /**
     * Execute a select statement and sort the result into a Map.
     * Usage is discouraged. Perform a direct query instead for higher efficiency and control.
     *
     * @param table    The Table to select from.
     * @param columns  The names of the columns you wish to select.
     * @param criteria The SQLite criteria, with the keys being the column names and the values to be matched via 'equals' (=)
     * @return the matched rows from the SQL table, with the column name as the key.
     */
    public Map<String, LinkedList<Object>> select(String table, List<String> columns, Map<String, Object> criteria) {
        Map<String, LinkedList<Object>> result = Maps.newHashMap();
        ResultSet res = null;

        String cols;
        if (columns == null) {
            cols = "*";
        } else {
            StringBuilder buffer = new StringBuilder(64);
            String div = "";
            for (String s : columns) {
                buffer.append(div);
                div = ",";
                buffer.append(s);
            }
            cols = buffer.toString();
        }

        String pretext = "SELECT " + cols + " FROM " + table;
        String query = pretext + SQLUtils.giveOptionalWhere(criteria);
        try {
            res = query(query);

            ResultSetMetaData rsmd = res.getMetaData();
            int count = rsmd.getColumnCount();
            LinkedList<LinkedList<Object>> data = new LinkedList<>();

            for (int i = 1; i <= count; i++) {
                String label = rsmd.getColumnLabel(i);
                LinkedList<Object> vals = Lists.newLinkedList();
                data.add(vals);
                result.put(label, vals);
            }

            while (res.next()) {
                for (int i = 1; i <= count; i++) {
                    data.get(i - 1).add(res.getObject(i));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            SQLUtils.closeStatement(res);
        }
        return result;
    }

    /**
     * Update one or more rows in the database
     *
     * @param table    The Table to update
     * @param values   The values to update, the keys of the maps being the column names.
     * @param criteria The criteria to match, the keys of the map serving as column names, and the values of the map the values which must match (an equals operation) the database row.
     */
    public void update(String table, Map<String, Object> values, Map<String, Object> criteria) {
        if (values == null) throw new NullPointerException("Columns to set cannot be null");

        String pretext = "UPDATE " + table + " SET ";
        String vals = SQLUtils.formatSetClause(values);

        String query = pretext + vals + SQLUtils.giveOptionalWhere(criteria);
        execute(query);
    }

    /**
     * Insert a new row into a database table
     *
     * @param table  The table to query
     * @param values The values to insert, the keys of the maps being the column names of the table.
     * @return the unique rowid of your inserted row
     */
    public int insert(String table, Map<String, Object> values) {
        ResultSet res = null;
        StringBuilder cols = new StringBuilder().append('(');
        StringBuilder vals = new StringBuilder().append('(');
        String prefix = "";

        //Create a query from the map of keys and values. Keys are the column IDs.
        for (Map.Entry<String, Object> e : values.entrySet()) {
            Object o = e.getValue();
            cols.append(prefix);
            vals.append(prefix);
            prefix = ",";
            cols.append(e.getKey());
            if (o == null) o = new Syntax("NULL");
            else if (o instanceof Boolean) o = ((Boolean) o) ? 1 : 0;
            else if (o instanceof String) o = ((String) o).replace(';', ' ').replace("'", "''");
            boolean noQuotes = (o instanceof Number) || (o instanceof Syntax);
            vals.append(noQuotes ? o.toString() : "'" + o.toString() + "'");
        }

        cols.append(')');
        vals.append(')');

        int result = -1;
        //State the Query
        String query = "INSERT INTO " + table + " " + cols.toString() + " VALUES " + vals.toString() + ";";
        try { //SQL Stuff
            synchronized (this) {
                query(query);
                res = query("SELECT last_insert_rowid() FROM " + table);
                if (res.next()) result = res.getInt("last_insert_rowid()");
                SQLUtils.closeStatement(res);
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } finally {
            SQLUtils.closeStatement(res);
        }

        return result;
    }

    /**
     * Delete all matching rows from a table
     *
     * @param table    The table to query
     * @param criteria The criteria to meet (with an equals operation) for a row to be removed, the map keys being the column names
     */
    public abstract void remove(String table, Map<String, Object> criteria);

    /**
     * Execute a SQLite statement for which no returning ResultSet is required.
     *
     * @param query The formatted string to create the statement with
     */
    public abstract void execute(String query);

    /**
     * @return The connection to the SQL database
     */
    public abstract Connection getConnection();

    /**
     * @return If this is an instance of a MySQLHandler
     */
    public boolean isMySql() {
        return (this instanceof WhySQLHandler);
    }
}
