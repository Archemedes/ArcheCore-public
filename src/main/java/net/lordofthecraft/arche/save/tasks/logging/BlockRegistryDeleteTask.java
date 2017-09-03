package net.lordofthecraft.arche.save.tasks.logging;

import net.lordofthecraft.arche.WeakBlock;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

public class BlockRegistryDeleteTask extends StatementTask {
    private final WeakBlock wb;

    public BlockRegistryDeleteTask(WeakBlock wb){
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
		return "DELETE FROM blockregistry WHERE world=? AND x=? AND y=? AND z=?";
	}
}
