package net.lordofthecraft.arche.save.tasks;

import net.lordofthecraft.arche.enums.ProfessionSlot;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.persona.ArchePersona;

import java.sql.SQLException;

/**
 * Created on 6/5/2017
 *
 * @author 501warhead
 */
public class UpdateSkillSlotTask extends StatementTask {

    private final ArchePersona persona;
    private final Skill skill;
    private final ProfessionSlot slot;

    public UpdateSkillSlotTask(ArchePersona persona, Skill skill, ProfessionSlot slot) {
        this.persona = persona;
        this.skill = skill;
        this.slot = slot;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setInt(1, slot.getSlot());
        stat.setString(2, persona.getPersonaId().toString());
        stat.setString(3, skill.getName());
    }

    @Override
    protected String getQuery() {
        return "UPDATE persona_skills SET skill_slot=? WHERE persona_id_fk=? AND skill_fk=?";
    }
}
