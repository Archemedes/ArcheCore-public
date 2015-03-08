package net.lordofthecraft.arche.save.tasks;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

public class DataTask extends ArcheTask {
	public static final int CREATE = 1;
	public static final int INSERT = 2;
	public static final int UPDATE = 3;
	public static final int DELETE = 4;
	
	private final int operation;
	private final String table;
	private final Map<String, Object> values;
	private final Map<String, Object> criteria;
	
	//private int hash = 0;
	
	public DataTask(int operation, String table, Map<String, Object> values, Map<String, Object> criteria){
		super();
			
		this.table = table;
		this.operation = operation;
		this.values = values;
		this.criteria = criteria;
		
	}
	
	public int getOperation(){
		return operation;
	}
	
	@Override
	public void run(){
		switch(operation){
		case CREATE:
			Map<String, String> newValues = Maps.newHashMap();
			
			for(Entry<String, Object> entry : criteria.entrySet())
				newValues.put(entry.getKey(), entry.getValue().toString());
			
			handle.createTable(table, newValues);
			break;
		case INSERT: handle.insert(table, values); break;
		case UPDATE: handle.update(table, values, criteria); break;
		case DELETE: handle.remove(table, criteria); break;
		default: throw new IllegalArgumentException("This operation type couldn't be executed");
		}
		
		return;
	}
	
	/*@Override
	public int hashCode(){
		if(hash == 0){
			hash = operation + (table == null? 0 : table.hashCode())
					+ (criteria == null? 0 : criteria.hashCode())
					+ (values == null? 0 : values.hashCode());
		}
		return hash;
	}
	
	@Override
	public boolean equals(Object o){
		if(o == null || !(o instanceof DataTask)) return false;
		
		DataTask with = (DataTask) o;
		
		if(operation != with.operation) return false;
		if(!StringUtils.equals(table, with.table)) return false;
		if(!ObjectUtils.equals(criteria, with.criteria)) return false;
		if(!ObjectUtils.equals(values, with.values)) return false;
		
		return true;
	}*/
	
}
