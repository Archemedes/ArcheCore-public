package net.lordofthecraft.arche.save.rows;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface RunnerRow extends ArcheRow {

	void run(Connection connection) throws SQLException;
	
	@Override
	default String[] getInserts() {
		return new String[0];
	}
}
