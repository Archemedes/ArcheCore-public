package net.lordofthecraft.arche.save.rows;

import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

public class FlexibleInsertRow extends FlexibleRow {
	
	
	public FlexibleInsertRow(String table, Mode mode) {
		super(table, mode.m);
	}
	
	@Override
	protected String getStatement() {
		
		String keys = vars.keySet().stream().collect(Collectors.joining(",", "(", ")"));
		String values = '(' + StringUtils.repeat("?", ",", vars.size()) + ')';
		return prefix() + keys + " VALUES" + values; 
	}

	@Override
	protected Object getValueFor(int index) {
		return valueAtIndex(index);
	}

	public enum Mode{
		INSERT("INSERT INTO"), REPLACE("REPLACE INTO"), IGNORE("INSERT " + orIgnore() + " INTO");
		private final String m;
		private Mode(String m) {
			this.m = m;
		}
	}
	
	@Override
	public FlexibleInsertRow set(String column, Object value) {
		return (FlexibleInsertRow) this.where(column, value);
	}
	
}
