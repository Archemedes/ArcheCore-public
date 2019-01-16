package net.lordofthecraft.arche.save.rows.skills;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class InsertSkillRow extends SingleStatementRow {
    final Persona persona;
    final Skill skill;
    final double xp;
    final boolean visible;

    public InsertSkillRow(Persona persona, Skill skill, double xp, boolean visible) {
        this.persona = persona;
        this.skill = skill;
        this.xp = xp;
        this.visible = visible;
    }

    @Override
    protected String getStatement() {
        return "INSERT INTO persona_skills (persona_id_fk,skill_id_fk,xp,visible) VALUES (?,?,?,?)";
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1:
                return persona.getPersonaId();
            case 2:
                return skill.getName();
            case 3:
                return xp;
            case 4:
                return visible;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return "SkillRow{" +
                "persona=" + persona.identify() +
                ", skill=" + skill.getName() +
                ", xp=" + xp +
                ", visible=" + visible +
                '}';
    }
}
