package net.lordofthecraft.arche.save.rows;

import java.sql.Connection;

@FunctionalInterface
public interface RunnerRow extends ArcheRow {

	void run(Connection connection);
	
	@Override
	default String[] getInserts() {
		return new String[0];
	}
}
