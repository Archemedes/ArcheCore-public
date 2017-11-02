package net.lordofthecraft.arche.save.rows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface StatementRow extends ArcheRow {

	public boolean isUnique();
	
	public PreparedStatement[] prepare(Connection connection) throws SQLException;
	
	public void setValues(PreparedStatement[] stats) throws SQLException;
}
