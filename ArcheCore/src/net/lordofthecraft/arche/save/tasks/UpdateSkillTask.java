package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.skill.*;
import java.sql.*;

public class UpdateSkillTask extends ArcheTask
{
    private final PreparedStatement stat;
    private final String uuid;
    private final int id;
    private final double xp;
    private final boolean visible;
    
    public UpdateSkillTask(final ArcheSkill skill, final String uuid, final int id, final double xp, final boolean visible) {
        super();
        this.uuid = uuid;
        this.id = id;
        this.stat = skill.getUpdateStatement();
        this.xp = xp;
        this.visible = visible;
    }
    
    @Override
    public void run() {
        try {
            this.stat.setString(1, this.uuid);
            this.stat.setInt(2, this.id);
            this.stat.setDouble(3, this.xp);
            this.stat.setBoolean(4, this.visible);
            synchronized (UpdateSkillTask.handle) {
                this.stat.execute();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
