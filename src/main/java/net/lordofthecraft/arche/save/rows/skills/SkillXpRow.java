package net.lordofthecraft.arche.save.rows.skills;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class SkillXpRow extends SingleStatementRow {
    private final Persona persona;
    private final Skill skill;
    private final double xp;

    public SkillXpRow(Persona persona, Skill skill, double xp) {
        this.persona = persona;
        this.skill = skill;
        this.xp = xp;
    }

    @Override
    protected String getStatement() {
        return "UPDATE persona_skills SET xp=? WHERE persona_id_fk=? AND skill_id_fk=?";
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1:
                return xp;
            case 2:
                return persona.getPersonaId();
            case 3:
                return skill.getName();
            default:
                throw new IllegalArgumentException();
        }
    }

}
