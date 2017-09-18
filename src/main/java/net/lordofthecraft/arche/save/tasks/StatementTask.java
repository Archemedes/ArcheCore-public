package net.lordofthecraft.arche.save.tasks;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public abstract class StatementTask extends ArcheTask {
	private static Map<String, PreparedStatement> stats = Maps.newHashMap();
	protected PreparedStatement stat = null;
    private static int count = 0;

    public StatementTask() {
		super();
	}
	
	@Override
	public void run(){
        ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
        int r = ++count;
        if (timer != null) {
            timer.startTiming("StatementTask " + simpleName() + " " + r);
        }
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
            conn.close();
        }catch(SQLException e){e.printStackTrace();}
        if (timer != null) {
            timer.stopTiming("StatementTask " + simpleName() + " " + r);
        }
    }
	
	private String simpleName(){
		return this.getClass().getSimpleName();
	}
	
	protected abstract void setValues() throws SQLException;
	
	protected abstract String getQuery();
}
