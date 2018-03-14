package net.lordofthecraft.arche.save.rows;

import java.util.LinkedHashMap;
import java.util.Map;

public class FlexibleUpdateRow extends FlexibleRow {
	protected final Map<String, Object> sets = new LinkedHashMap<>();
	
	public FlexibleUpdateRow(String table) {
		super(table, "UPDATE");
	}

	@Override
	protected String getStatement() {
		return prefix() + "SET " + setFromVars(sets) + " WHERE " + setFromVars(); 
	}

	@Override
	protected Object getValueFor(int index) {
		if(index <= 0 ) throw new IllegalArgumentException();
		
		if(index <= sets.size()) {
			return valueAtIndex(sets, index);
		} else if (index <= sets.size() + vars.size()) {
			return valueAtIndex(vars, index-sets.size());
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public FlexibleUpdateRow where(String column, Object value) {
		return (FlexibleUpdateRow) super.where(column, value);
	}
	
	public FlexibleUpdateRow set(String column, Object value) {
		sets.put(column, value);
		return this;
	}
	
}
