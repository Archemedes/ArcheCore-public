package net.lordofthecraft.arche.save.rows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang.Validate;

public abstract class MultiStatementRow extends StatementRow {

	private PreparedStatement[] statements;
	
	@Override
	public boolean isUnique() {
		return false;
	}
	
	@Override
	public final PreparedStatement[] prepare(Connection connection) throws SQLException {
		PreparedStatement[] result = (isUnique() || statements == null || statements[0].isClosed())?
				prepareInternal(connection) : statements;
		
		if(!isUnique()) statements = result;
		return result;
	}
	
	private PreparedStatement[] prepareInternal(Connection connection) throws SQLException{
		String[] sql = getStatements();
		Validate.isTrue(sql.length > 0);
		PreparedStatement[] result = new PreparedStatement[sql.length];
		
		for(int h = 0; h<sql.length;h++) {
			PreparedStatement s = connection.prepareStatement(sql[h]);
			statPool.add(s);
			result[h] = s;
		}
		
		return result;
	}
	

}
