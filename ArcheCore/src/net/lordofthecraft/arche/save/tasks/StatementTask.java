package net.lordofthecraft.arche.save.tasks;

import java.util.*;
import java.sql.*;
import com.google.common.collect.*;

public abstract class StatementTask extends ArcheTask
{
    private static Map<String, PreparedStatement> stats;
    protected PreparedStatement stat;
    
    public StatementTask() {
        super();
        this.stat = null;
    }
    
    @Override
    public void run() {
        try {
            final Connection conn = StatementTask.handle.getSQL().getConnection();
            final String n = this.simpleName();
            this.stat = StatementTask.stats.get(n);
            if (this.stat == null) {
                this.stat = conn.prepareStatement(this.getQuery());
                StatementTask.stats.put(n, this.stat);
            }
            this.setValues();
            this.stat.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private String simpleName() {
        return this.getClass().getSimpleName();
    }
    
    protected abstract void setValues() throws SQLException;
    
    protected abstract String getQuery();
    
    static {
        StatementTask.stats = Maps.newHashMap();
    }
}
