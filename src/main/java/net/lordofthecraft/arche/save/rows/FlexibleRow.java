package net.lordofthecraft.arche.save.rows;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.lordofthecraft.arche.util.SQLUtil;

//Because duck your overcomplicated optimizations of BS that runs async
public abstract class FlexibleRow extends SingleStatementRow {
	protected final String table;
	protected final String operation;
	
	protected final Map<String, Object> vars = new LinkedHashMap<>();
	
	public FlexibleRow(String table, String operation) {
		this.table = table;
		this.operation = operation;
	}
	
	@Override
	public final boolean isUnique() {
		return true;
	}
	
	protected String prefix() {
		return operation + ' ' + table + ' ';
	}
	
	protected String setFromVars() {
		return setFromVars(vars);
	}
	
	protected String setFromVars(Map<String, Object> x) {
		return x.keySet().stream().map(SQLUtil::mysqlTextEscape).collect(Collectors.joining("=?,", "", "=?"));
	}
	
	protected Object valueAtIndex(int index) {
		return valueAtIndex(vars, index);
	}
	
	protected Object valueAtIndex(Map<String, Object> x, int index) {
		if(index > vars.size()) throw new IllegalArgumentException();
		
		Iterator<Entry<String, Object>> it = x.entrySet().iterator();
		for(int i = 1; i < index; i++) it.next();
		return it.next();
	}
	

	
	public FlexibleRow where(String column, Object value) {
		vars.put(column, value);
		return this;
	}
}