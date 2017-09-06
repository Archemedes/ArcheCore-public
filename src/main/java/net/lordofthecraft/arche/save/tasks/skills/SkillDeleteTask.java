package net.lordofthecraft.arche.save.tasks.skills;

import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Deletes a skill from {@code 'persona_skills'}
 * Only happens in SQL. This is called in {@link net.lordofthecraft.arche.persona.PersonaSkills} on SQLUpdate
 *
 * @author 501warhead
 */
public class SkillDeleteTask extends StatementTask{

    private final String toDelete;
    private final UUID persona_id;

    public SkillDeleteTask(String toDelete, UUID persona_id) {
        this.toDelete = toDelete;
        this.persona_id = persona_id;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, persona_id.toString());
        stat.setString(2, toDelete);
    }

    @Override
    protected String getQuery() {
        return "DELETE FROM persona_skills WHERE persona_id_fk=? AND skill_id_fk=?";
    }
}
