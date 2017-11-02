package net.lordofthecraft.arche.save.rows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.util.SQLUtil;

public abstract class MultiStatementRow implements StatementRow {
	private static Set<PreparedStatement> statPool = identityHashSet();
	private PreparedStatement[] statements;
	
	public static void close() { //Called by consumer
		statPool.forEach(t -> {
			try {t.close();} 
			catch (SQLException e) {e.printStackTrace();}
		});
		
		statPool = identityHashSet();
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
				if(o instanceof Number || o instanceof Boolean || o instanceof Timestamp) {
					result.append(o.toString());
				} else { //String, UUID, enum
					result.append('\'').append(SQLUtil.mysqlTextEscape(o.toString())).append('\'');
				}
			}
			
			finalResult[h] = result.toString();
		}
		
		return finalResult;
	}
	
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
	
	@Override
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
						statement.setInt(i, ((Number) o).intValue());
						ArcheCore.getPlugin().getLogger().warning("unhandled Number implementation being used: " + o.getClass().getName() + ". Int assumed");
					}
				} else if(o instanceof Timestamp) {
					statement.setTimestamp(i, (Timestamp) o);
				} else if(o instanceof Boolean) {
					statement.setBoolean(i, (Boolean) o);
				} else { //String, enums, uuid
					statement.setString(i, o==null? null : o.toString());
				}
			}
		}
	}
	
	private static Set<PreparedStatement> identityHashSet(){
		return Collections.newSetFromMap(new IdentityHashMap<>());
	}
	
	public static boolean usingSQLite() {
		return ArcheCore.getControls().isUsingSQLite();
	}
	
	public static String orIgnore() {
		return usingSQLite()? "IGNORE" : "OR IGNORE"; 
	}
	
	public static Timestamp now() {
		return new Timestamp(System.currentTimeMillis());
	}
	
	protected abstract String[] getStatements();
	
	protected abstract Object getValueFor(int statement, int varIndex);
}
