package net.lordofthecraft.arche.save.rows.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class DeletePersonaTagRow extends SingleStatementRow {
    private final Persona persona;
    private final String key;

    public DeletePersonaTagRow(Persona persona, String key) {
        this.persona = persona;
        this.key = key;
    }
    
	@Override
	protected String getStatement() {
		return "DELETE FROM persona_tags WHERE persona_id_fk=? AND tag_key=?";
	}

	@Override
	protected Object getValueFor(int index) {
		switch(index) {
		case 1: return persona.getPersonaId();
		case 2: return key;
		default: throw new IllegalArgumentException();
		}
	}

}
