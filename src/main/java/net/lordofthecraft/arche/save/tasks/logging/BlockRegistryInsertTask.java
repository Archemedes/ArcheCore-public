package net.lordofthecraft.arche.save.tasks.logging;

import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.save.tasks.StatementTask;
import net.lordofthecraft.arche.util.WeakBlock;

import java.sql.SQLException;

public class BlockRegistryInsertTask extends StatementTask {
	private final WeakBlock wb;
	
	public BlockRegistryInsertTask(WeakBlock wb){
		this.wb = wb;
	}
	
	@Override
	protected void setValues() throws SQLException {
		stat.setString(1, wb.getWorld());
		stat.setInt(2, wb.getX());
		stat.setInt(3, wb.getY());
		stat.setInt(4, wb.getZ());
	}

	@Override
	protected String getQuery() {
        if (handle instanceof ArcheSQLiteHandler) {
            return "INSERT OR IGNORE INTO blockregistry VALUES (?,?,?,?)";
        }
        return "INSERT IGNORE INTO blockregistry VALUES (?,?,?,?)";
    }

}
