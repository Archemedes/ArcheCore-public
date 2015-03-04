package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.*;
import java.sql.*;

public class BlockRegistryDeleteTask extends StatementTask
{
    private final WeakBlock wb;
    
    public BlockRegistryDeleteTask(final WeakBlock wb) {
        super();
        this.wb = wb;
    }
    
    @Override
    protected void setValues() throws SQLException {
        this.stat.setString(1, this.wb.getWorld());
        this.stat.setInt(2, this.wb.getX());
        this.stat.setInt(3, this.wb.getY());
        this.stat.setInt(4, this.wb.getZ());
    }
    
    @Override
    protected String getQuery() {
        return "DELETE FROM blockregistry WHERE world=? AND x=? AND y=? AND z=?";
    }
}
