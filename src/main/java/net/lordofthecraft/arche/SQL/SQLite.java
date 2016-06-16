package net.lordofthecraft.arche.SQL;

import java.io.Closeable;
import java.io.File;
import java.sql.*;
import java.util.logging.Logger;

interface StatementsList {} //Simple tagging. Might need public modifier.

public /*abstract*/ class SQLite implements Closeable {

    protected Logger logger;
    protected Connection connection;
    //protected DBList driver;
    protected String prefix;
    // protected String dbprefix;
    @SuppressWarnings("unused")
    private int lastUpdate; //Exclusively a debug tool
    //private volatile Object syncObject = new Object();
    //private volatile boolean shouldWait = false;

    private SQLiteUtils utils; //Port

    public SQLite(Logger logger, String prefix, String directory, String filename) //constructor
    {
        if(logger == null)
        {
            Logger.getLogger("SimpleSQL").severe("logger cannot be null!");
            return;
        }
        if(prefix == null)
        {
            Logger.getLogger("SimpleSQL").severe("prefix cannot be null!");
            return;
        }
        this.prefix = prefix;
        this.logger = logger;
        this.utils = new SQLiteUtils(this);
        setFile(directory, filename);
    }

    public boolean open() //Overridden
    {
        if (initialize())
        {
            try
            {
                this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.getFile().getAbsolutePath());
                return true;
            }
            catch (SQLException e)
            {
                this.printError("Could not establish an SQLite connection, SQLException: " + e.getMessage());
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public final void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                this.printError("Could not close connection, SQLException: " + e.getMessage());
            }
        } else {
            this.printError("Could not close connection, it is null.");
        }
    }

    public final Connection getConnection() {
        return this.connection;
    }

    public final boolean isOpen() {
        if (connection != null)
            try {
                if (connection.isValid(1))
                    return true;
            } catch (SQLException e) {}
        return false;
    }

    public final boolean isOpen(int seconds) {
        if (connection != null)
            try {
                if (connection.isValid(seconds))
                    return true;
            } catch (SQLException e) {}
        return false;
    }

    protected void printError(String error)
    {
        logger.severe(this.prefix+ "[SQL]" + error);
    }

    public Statements getStatement(String query) throws SQLException
    {
        String[] statement = query.trim().split(" ", 2);
        try
        {
            Statements converted = Statements.valueOf(statement[0].toUpperCase());
            return converted;
        }
        catch (IllegalArgumentException e)
        {
            throw new SQLException("Unknown statement: \"" + statement[0] + "\".");
        }
    }

    protected void queryValidation(StatementsList statement) throws SQLException { }

    //Function that talks with the database
    public final ResultSet query(String query) throws SQLException {
        //doWait();
        queryValidation(this.getStatement(query));
        Statement statement = this.getConnection().createStatement();
        if (statement.execute(query)) {
            //this.shouldWait = true;
            return statement.getResultSet();
        } else {
            //int uc = statement.getUpdateCount();
            statement.close();
            //this.lastUpdate = uc;
            //return this.getConnection().createStatement().executeQuery("SELECT " + uc);
            return null;
        }
    }

    protected boolean initialize() //Port
    {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        } catch (ClassNotFoundException e) {
            this.printError("Class not found in initialize(): " + e);
            return false;
        }
    }

    private File getFile() //You get the point
    {
        return utils.getFile();
    }

    private void setFile(String directory, String filename) {
        utils.setFile(directory, filename);
    }

    @SuppressWarnings("unused")
    private void setFile(String directory, String filename, String extension) {
        utils.setFile(directory, filename, extension);
    }

    private enum Statements implements StatementsList //SQLite sub port
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

        Statements(String string)
        {
            this.string = string;
        }

        public String toString()
        {
            return string;
        }
    }
        
        /*public Object getSynchronized()
        {
                return this.syncObject;
        }
        
        public boolean shouldWait()
        {
                return this.shouldWait;
        }
        
        public void doWait()
        {
                if(shouldWait())
                {
                        try {
                                synchronized (getSynchronized())
                                {
                                        getSynchronized().wait();
                                }
                        } catch (InterruptedException e) {
                                return;
                        }
                }
                else
                {
                        return;
                }
        }


        public void setShouldWait(boolean b) {
                this.shouldWait = b;
        }
        */
}
       


