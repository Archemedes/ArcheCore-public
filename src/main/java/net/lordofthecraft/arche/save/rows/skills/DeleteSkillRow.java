package net.lordofthecraft.arche.save.rows.skills;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class DeleteSkillRow extends SingleStatementRow {
    final int persona;
    final Skill skill;

    public DeleteSkillRow(Persona persona, Skill skill) {
        this.persona = persona.getPersonaId();
        this.skill = skill;
    }
	@Override
	protected String getStatement() {
		return "DELETE FROM persona_skills WHERE persona_id_fk=? AND skill_id_fk=?";
	}

	@Override
	protected Object getValueFor(int index) {
		switch(index) {
		case 1: return persona;
		case 2: return skill.getName();
		default: throw new IllegalArgumentException();
		}
	}
}