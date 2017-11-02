package net.lordofthecraft.arche.save.rows.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.MultiStatementRow;

public class DeletePersonaRow extends MultiStatementRow {
	private final Persona persona;
	
	public DeletePersonaRow(Persona persona) {
		this.persona = persona;
	}
	
	@Override
	protected String[] getStatements() {
		return new String[]{
	            "DELETE FROM persona_skills WHERE persona_id_fk=?",
	            "DELETE FROM persona_magics WHERE persona_id_fk=?",
	            "DELETE FROM persona_vitals WHERE persona_id_fk=?",
	            "DELETE FROM persona_stats WHERE persona_id_fk=?",
	            "DELETE FROM persona_tags WHERE persona_id_fk=?",
	            "DELETE FROM persona_name WHERE persona_id_fk=?",
	            "DELETE FROM persona_attributes WHERE persona_id_fk=?",
	            "DELETE FROM per_persona_skins WHERE persona_id_fk=?",
	            "DELETE FROM persona WHERE persona_id=?"
	    };
	}

	@Override
	protected Object getValueFor(int statement, int varIndex) {
		return persona.getPersonaId();
	}

}
