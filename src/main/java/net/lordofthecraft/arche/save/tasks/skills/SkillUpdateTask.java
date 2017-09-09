package net.lordofthecraft.arche.save.tasks.skills;

import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.persona.SkillAttachment;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

/**
 * Created on 9/4/2017
 *
 * @author 501warhead
 */
public class SkillUpdateTask extends StatementTask {

    private final int persona_id;
    private final String skill;
    private final SkillAttachment.Field field;
    private final Object value;

    public SkillUpdateTask(int persona_id, String skill, SkillAttachment.Field field, Object value) {
        this.persona_id = persona_id;
        this.skill = skill;
        this.field = field;
        this.value = value;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, field.field);
        if (handle instanceof ArcheSQLiteHandler) {
            switch (field) {
                case XP:
                    stat.setDouble(2, (double) value);
                    break;
                case VISIBLE:
                    stat.setBoolean(2, (boolean) value);
                    break;
            }
        } else {
            stat.setObject(2, value, field.type);
        }
        stat.setInt(3, persona_id);
        stat.setString(4, skill);
    }

    @Override
    protected String getQuery() {
        return "UPDATE persona_skills SET ?=? WHERE persona_id_fk=? AND skill_id_fk=?";
    }
}
