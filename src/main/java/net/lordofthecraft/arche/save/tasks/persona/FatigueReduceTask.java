package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.tasks.ArcheTask;

/**
 * Created on 9/3/2017
 *
 * @author 501warhead
 */
public class FatigueReduceTask extends ArcheTask {

    public FatigueReduceTask() {
    }

    @Override
    public void run() {
        ArcheCore.getSQLControls().execute("UPDATE persona SET fatigue=fatigue-1 WHERE fatigue>0");
    }
}
