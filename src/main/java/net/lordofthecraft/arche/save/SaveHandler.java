package net.lordofthecraft.arche.save;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.tasks.ArcheTask;

public class SaveHandler {

    //private BlockingDeque<ArcheTask> saveQueue = new LinkedBlockingDeque<ArcheTask>();
    //private SaveExecutorManager manager = new SaveExecutorManager();

	public static SaveHandler getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	public boolean isEmpty(){
        return false;
    }
	
	public void put(ArcheTask s){
		try{
            ArcheCore.getControls().getSaveManager().submit(s);
        }catch(IllegalStateException e ){
			throw new IllegalStateException("ArcheCore save buffer is full! Task rejected.",e);
		}
	}

	/*public ArcheTask take(){
        try{
			return saveQueue.take();
		} catch (InterruptedException e) {e.printStackTrace();}

		return null;
	}*/

    private static class SingletonHolder {
		private static final SaveHandler INSTANCE = new SaveHandler();
	}
}
