package net.lordofthecraft.arche.save.rows.skills;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class SkillVisibleRow extends SingleStatementRow {
    private final Persona persona;
    private final Skill skill;
    private final boolean visible;

    public SkillVisibleRow(Persona persona, Skill skill, boolean visible) {
        this.persona = persona;
        this.skill = skill;
        this.visible = visible;
    }

    @Override
    protected String getStatement() {
        return "UPDATE persona_skills SET visible=? WHERE persona_id_fk=? AND skill_id_fk=?";
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1:
                return visible;
            case 2:
                return persona.getPersonaId();
            case 3:
                return skill.getName();
            default:
                throw new IllegalArgumentException();
        }
    }
}
