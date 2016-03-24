package net.lordofthecraft.arche.save;

import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.save.tasks.ArcheTask;
import net.lordofthecraft.arche.save.tasks.EndOfStreamTask;
import org.bukkit.Bukkit;

public class DataSaveRunnable implements Runnable{	
	private final SaveHandler queue;
	private final ArcheTimer timer;
	private final SQLHandler handle;
			
	public DataSaveRunnable(SaveHandler queue, ArcheTimer timer, SQLHandler handle){
		this.queue  = queue  ;
		this.timer  = timer  ;
		this.handle = handle ;
	}
	
	@Override
	public void run() {
		while(true){
			try{
				ArcheTask task = queue.take(); //This waits until a task is found.
				
				int i = 0;
				handle.execute("BEGIN");
				if(timer != null) Bukkit.getLogger().info("[ArcheCore][Debug] Starting an ArcheCore SaveHandler transaction now.");
				do{
					if(timer != null) timer.startTiming(task.getClass().getSimpleName());
					task.run();
					if(timer != null) timer.stopTiming(task.getClass().getSimpleName());

					if(task instanceof EndOfStreamTask){ //Task Kills the consumer	
						return; //Transaction already ended by the EOStask.run()
					}
					
					if(i++ >= 1000) task = null;
					else task = queue.poll();
				}while(task != null);
				handle.execute("END TRANSACTION");
			}catch(Exception e){	  				//Report possible errors from executing tasks
				e.printStackTrace() 			  ; //Provide nature of the error
				handle.execute("END TRANSACTION") ; //Ends transaction
				continue						  ; //Try to continue as normal
			}
		}
	}
}
