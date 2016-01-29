package net.lordofthecraft.arche.save;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import net.lordofthecraft.arche.save.tasks.ArcheTask;

public class SaveHandler {

	private BlockingDeque<ArcheTask> saveQueue = new LinkedBlockingDeque<ArcheTask>();
	
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
	
	public ArcheTask poll(){
		return saveQueue.poll();
	}
	
	public static SaveHandler getInstance(){
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		private static final SaveHandler INSTANCE = new SaveHandler();
	}
}
