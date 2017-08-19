package net.lordofthecraft.arche.save;

import net.lordofthecraft.arche.save.tasks.ArcheTask;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class SaveHandler {

	private BlockingDeque<ArcheTask> saveQueue = new LinkedBlockingDeque<>();

	public static SaveHandler getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public boolean isEmpty(){
		return saveQueue.isEmpty();
	}
	
	public void put(ArcheTask s){
		try{
			saveQueue.add(s);
		}catch(IllegalStateException e ){
			throw new IllegalStateException("ArcheCore save buffer is full! Task rejected.",e);
		}
	}

	public ArcheTask take(){
		try{
			return saveQueue.take();
		} catch (InterruptedException e) {e.printStackTrace();}

		return null;
	}

	public boolean contains(ArcheTask task) {
        return saveQueue.contains(task);
    }

	public ArcheTask poll(){
		return saveQueue.poll();
	}
	
	private static class SingletonHolder {
		private static final SaveHandler INSTANCE = new SaveHandler();
	}
}
