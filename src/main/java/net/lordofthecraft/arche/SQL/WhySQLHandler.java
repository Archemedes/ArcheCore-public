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
import net.lordofthecraft.arche.util.SQLUtil;

/**
 * Represents an SQLHandler affiliated with MySQL
 *
 * @author 501warhead (Why)
 */
public class WhySQLHandler extends SQLHandler {

    private final MySQLConnectionPool pool;

    public WhySQLHandler(String user, String password) throws ClassNotFoundException {
        pool = new MySQLConnectionPool(getUrl(), user, password);
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

    /*@Override
    public void createTable(String table, Map<String, String> criteria) {
        StringBuilder buffer = new StringBuilder(64);
        String pretext = "CREATE TABLE IF NOT EXISTS " + table + " ";

        String div = "";
        for(Map.Entry<String, String> entry : criteria.entrySet()){
            buffer.append(div);
            div = ", ";
            buffer.append(entry.getKey()).append(" ");
            buffer.append(entry.getValue().toUpperCase());
        }

        //System.out.println(pretext + "(" + buffer.toString() + ");");
        execute(pretext + "(" + buffer.toString() + ");");
    }*/

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

    /*@Override
    public Map<String, LinkedList<Object>> select(String table, List<String> columns, Map<String, Object> criteria) {
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
            res = query(query);

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
    }

    @Override
    public void update(String table, Map<String, Object> values, Map<String, Object> criteria) {
        if(values == null) throw new NullPointerException("Columns to set cannot be null");

        String pretext = "UPDATE " + table + " SET ";
        String vals = SQLUtils.formatSetClause(values);

        String query = pretext + vals + SQLUtils.giveOptionalWhere(criteria);
        execute(query);
    }

    @Override
    public int insert(String table, Map<String, Object> values) {
        ResultSet res = null;
        StringBuilder cols = new StringBuilder().append('(');
        StringBuilder vals = new StringBuilder().append('(');
        String prefix = "";

        //Create a query from the map of keys and values. Keys are the column IDs.
        for(Map.Entry<String,Object> e : values.entrySet()){
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
                res = query("SELECT last_insert_rowid() FROM " + table);
                if(res.next()) result = res.getInt("last_insert_rowid()");
                SQLUtils.closeStatement(res);
            }
        } catch(SQLException se){se.printStackTrace();}
        finally{SQLUtils.closeStatement(res);}

        return result;
    }*/

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
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "Failed to get the MySQL connection!", e);
            return null;
        }
    }
}
