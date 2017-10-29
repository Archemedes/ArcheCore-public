package net.lordofthecraft.arche.save.rows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.util.SQLUtil;

public abstract class StatementRow implements ArcheRow {
	private final boolean usingSqlite;
	
	
	public StatementRow(){
		usingSqlite = ArcheCore.getSQLControls() instanceof ArcheSQLiteHandler;
	}
	
	@Override
	public String[] getInserts() { //Works in most cases else override
		String sql[] = getStatements();
		String finalResult[] = new String[sql.length];
		
		for(int h = 0; h < sql.length; h++) {
			String[] bits = sql[h].split("\\?");
			StringBuilder result = new StringBuilder();
			for(int i = 1; i <= bits.length; i++){
				result.append(bits[i-1]);

				Object o = getValueFor(h,i);
				if(o instanceof String) {
					result.append('\'').append(SQLUtil.mysqlTextEscape((String) o)).append('\'');
				} else {
					result.append(o.toString());
				}
			}
			
			finalResult[h] = result.toString();
		}
		
		return finalResult;
	}
	
	public boolean isUnique() {
		return false;
	}
	
	public final PreparedStatement[] prepare(Connection connection) throws SQLException {
		String[] sql = getStatements();
		PreparedStatement[] result = new PreparedStatement[sql.length];
		
		for(int h = 0; h<sql.length;h++) {
			result[h] = connection.prepareStatement(sql[h]);
		}
		
		return result;
	}
	
	public final void setValues(PreparedStatement[] statements) throws SQLException {
		String[] sqls = getStatements();
		
		for(int h = 0; h < sqls.length; h++) {
			String sql = sqls[h];
			PreparedStatement statement = statements[h];
			int amountOfVariables = StringUtils.countMatches(sql, "?");
			for(int i = 1; i <= amountOfVariables; i++) {
				Object o = getValueFor(h,i);
				
				if(o instanceof Number) { //Numbers in order of decreasing likelihood
					if(o instanceof Integer) {
						statement.setInt(i, (Integer) o);
					} else if(o instanceof Double) {
						statement.setDouble(i, (Double) o);
					} else if(o instanceof Long) {
						statement.setLong(i, (Long) o);
					} else if(o instanceof Float) {
						statement.setFloat(i, (Float) o);
					} else if(o instanceof Short) {
						statement.setShort(i, (Short) o);
					} else if (o instanceof Byte) {
						statement.setByte(i, (Byte) o);
					} else {
						throw new IllegalArgumentException("Custom Number implementation!");
					}
				} else if(o instanceof Timestamp) {
					statement.setTimestamp(i, (Timestamp) o);
				} else { //String, enum, etc
					statement.setString(i, o.toString());
				}
			}
		}
	}
	
	public final boolean isUsingSqlite() { return usingSqlite; }
	
	protected abstract String[] getStatements();
	
	protected abstract Object getValueFor(int statement, int varIndex);
}
