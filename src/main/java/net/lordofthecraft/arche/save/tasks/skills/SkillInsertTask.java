package net.lordofthecraft.arche.save.tasks.skills;

import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

public class SkillInsertTask extends StatementTask {

    private final int persona_id;
    private final String skill_name;
    private final double xp;
    private final boolean visible;
    private final boolean primary;

    public SkillInsertTask(int persona_id, String skill_name, double xp, boolean visible, boolean primary) {
        this.persona_id = persona_id;
        this.skill_name = skill_name;
        this.xp = xp;
        this.visible = visible;
        this.primary = primary;
    }

    @Override
    protected void setValues() throws SQLException {

    }

    @Override
    protected String getQuery() {
        return "INSERT INTO persona_skills";
    }
}
