package net.lordofthecraft.arche.save.tasks;

import com.google.common.collect.Maps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public abstract class StatementTask extends ArcheTask {
	private static Map<String, PreparedStatement> stats = Maps.newHashMap();
	protected PreparedStatement stat = null;
	
	public StatementTask() {
		super();
	}
	
	@Override
	public void run(){
		try{
			Connection conn = handle.getConnection();
			String n = simpleName();
			stat = stats.get(n);
			if(stat == null /*|| stat.isClosed()*/){
				stat = conn.prepareStatement(getQuery());
				stats.put(n, stat);
			}
			
			setValues();
			stat.execute();
		}catch(SQLException e){e.printStackTrace();}
	}
	
	private String simpleName(){
		return this.getClass().getSimpleName();
	}
	
	protected abstract void setValues() throws SQLException;
	
	protected abstract String getQuery();
}
