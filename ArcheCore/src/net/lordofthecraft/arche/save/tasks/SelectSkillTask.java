package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.persona.*;
import net.lordofthecraft.arche.interfaces.*;
import net.lordofthecraft.arche.skill.*;
import java.util.concurrent.*;
import net.lordofthecraft.arche.*;

public class SelectSkillTask extends ArcheTask
{
    private final FutureTask<SkillData> future;
    
    public SelectSkillTask(final ArchePersona persona, final Skill s) {
        super();
        final SkillDataCallable callable = new SkillDataCallable(persona, s.getName(), SelectSkillTask.handle);
        this.future = new FutureTask<SkillData>(callable);
        if (ArcheCore.getPlugin().debugMode()) {
            ArcheCore.getPlugin().getLogger().info("[Debug] Now creating a SelectSkillTask for " + persona.getPlayerName() + " and skill " + s.getName());
        }
    }
    
    public FutureTask<SkillData> getFuture() {
        return this.future;
    }
    
    @Override
    public void run() {
        this.future.run();
    }
}
