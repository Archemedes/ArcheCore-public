package net.lordofthecraft.arche.save.tasks;

import java.sql.SQLException;

import net.lordofthecraft.arche.WeakBlock;

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
		return "INSERT INTO blockregistry VALUES (?,?,?,?)";
	}

}
