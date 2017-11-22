package net.lordofthecraft.arche.save.rows.persona;

import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.save.rows.MultiStatementRow;

public class DeletePersonaRow extends MultiStatementRow {
	private final int persona;
	
	public DeletePersonaRow(OfflinePersona persona) {
		this.persona = persona.getPersonaId();
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
	            "DELETE FROM persona WHERE persona_id=?"
	    };
	}

	@Override
	protected Object getValueFor(int statement, int varIndex) {
		return persona;
	}

}
