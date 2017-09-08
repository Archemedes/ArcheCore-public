package net.lordofthecraft.arche.save.tasks.skills;

import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;

/**
 * Deletes a skill from {@code 'persona_skills'}
 * Only happens in SQL. This is called in {@link net.lordofthecraft.arche.persona.PersonaSkills} on SQLUpdate
 *
 * @author 501warhead
 */
public class SkillDeleteTask extends StatementTask{

    private final String toDelete;
    private final int persona_id;

    public SkillDeleteTask(String toDelete, int persona_id) {
        this.toDelete = toDelete;
        this.persona_id = persona_id;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setInt(1, persona_id);
        stat.setString(2, toDelete);
    }

    @Override
    protected String getQuery() {
        return "DELETE FROM persona_skills WHERE persona_id_fk=? AND skill_id_fk=?";
    }
}
