package net.lordofthecraft.arche.save.tasks.skills;

import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;
import java.util.UUID;

public class SkillInsertTask extends StatementTask {

    private final UUID persona_id;
    private final String skill_name;
    private final double xp;
    private final boolean visible;

    public SkillInsertTask(UUID persona_id, String skill_name, double xp, boolean visible) {
        this.persona_id = persona_id;
        this.skill_name = skill_name;
        this.xp = xp;
        this.visible = visible;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, persona_id.toString());
        stat.setString(2, skill_name);
        stat.setDouble(3, xp);
        stat.setBoolean(4, visible);
    }

    @Override
    protected String getQuery() {
        return "INSERT INTO persona_skills (persona_id_fk,skill_id_fk,xp,visible) VALUES (?,?,?,?)";
    }
}
