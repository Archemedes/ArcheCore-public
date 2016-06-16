package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;

public abstract class ArcheTask implements Runnable {
	protected static SQLHandler handle = null;
	
	protected ArcheTask(){
		if(handle == null)
			handle = ArcheCore.getPlugin().getSQLHandler();
	}
	
	@Override
	public abstract void run();

}
