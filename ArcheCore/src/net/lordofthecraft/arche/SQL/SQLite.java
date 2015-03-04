package net.lordofthecraft.arche.SQL;

import java.util.logging.*;
import java.sql.*;
import java.io.*;

public class SQLite
{
    protected Logger logger;
    protected Connection connection;
    protected String prefix;
    private int lastUpdate;
    private SQLiteUtils utils;
    
    public SQLite(final Logger logger, final String prefix, final String directory, final String filename) {
        super();
        if (logger == null) {
            Logger.getLogger("SimpleSQL").severe("logger cannot be null!");
            return;
        }
        if (prefix == null) {
            Logger.getLogger("SimpleSQL").severe("prefix cannot be null!");
            return;
        }
        this.prefix = prefix;
        this.logger = logger;
        this.utils = new SQLiteUtils(this);
        this.setFile(directory, filename);
    }
    
    public boolean open() {
        if (this.initialize()) {
            try {
                this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.getFile().getAbsolutePath());
                return true;
            }
            catch (SQLException e) {
                this.printError("Could not establish an SQLite connection, SQLException: " + e.getMessage());
                return false;
            }
        }
        return false;
    }
    
    public final boolean close() {
        if (this.connection != null) {
            try {
                this.connection.close();
                return true;
            }
            catch (SQLException e) {
                this.printError("Could not close connection, SQLException: " + e.getMessage());
                return false;
            }
        }
        this.printError("Could not close connection, it is null.");
        return false;
    }
    
    public final Connection getConnection() {
        return this.connection;
    }
    
    public final boolean isOpen() {
        if (this.connection != null) {
            try {
                if (this.connection.isValid(1)) {
                    return true;
                }
            }
            catch (SQLException ex) {}
        }
        return false;
    }
    
    public final boolean isOpen(final int seconds) {
        if (this.connection != null) {
            try {
                if (this.connection.isValid(seconds)) {
                    return true;
                }
            }
            catch (SQLException ex) {}
        }
        return false;
    }
    
    protected void printError(final String error) {
        this.logger.severe(this.prefix + "[SQL]" + error);
    }
    
    public Statements getStatement(final String query) throws SQLException {
        final String[] statement = query.trim().split(" ", 2);
        try {
            final Statements converted = Statements.valueOf(statement[0].toUpperCase());
            return converted;
        }
        catch (IllegalArgumentException e) {
            throw new SQLException("Unknown statement: \"" + statement[0] + "\".");
        }
    }
    
    protected void queryValidation(final StatementsList statement) throws SQLException {
    }
    
    public final ResultSet query(final String query) throws SQLException {
        this.queryValidation(this.getStatement(query));
        final Statement statement = this.getConnection().createStatement();
        if (statement.execute(query)) {
            return statement.getResultSet();
        }
        statement.close();
        return null;
    }
    
    protected boolean initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        }
        catch (ClassNotFoundException e) {
            this.printError("Class not found in initialize(): " + e);
            return false;
        }
    }
    
    private File getFile() {
        return this.utils.getFile();
    }
    
    private void setFile(final String directory, final String filename) {
        this.utils.setFile(directory, filename);
    }
    
    private void setFile(final String directory, final String filename, final String extension) {
        this.utils.setFile(directory, filename, extension);
    }
    
    private enum Statements implements StatementsList
    {
        SELECT("SELECT"), 
        INSERT("INSERT"), 
        UPDATE("UPDATE"), 
        DELETE("DELETE"), 
        REPLACE("REPLACE"), 
        CREATE("CREATE"), 
        ALTER("ALTER"), 
        DROP("DROP"), 
        ANALYZE("ANALYZE"), 
        ATTACH("ATTACH"), 
        BEGIN("BEGIN"), 
        DETACH("DETACH"), 
        END("END"), 
        EXPLAIN("EXPLAIN"), 
        INDEXED("INDEXED"), 
        PRAGMA("PRAGMA"), 
        REINDEX("REINDEX"), 
        RELEASE("RELEASE"), 
        SAVEPOINT("SAVEPOINT"), 
        VACUUM("VACUUM"), 
        LINE_COMMENT("--"), 
        BLOCK_COMMENT("/*");
        
        private String string;
        
        private Statements(final String string) {
            this.string = string;
        }
        
        @Override
        public String toString() {
            return this.string;
        }
    }
}
