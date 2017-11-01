package net.lordofthecraft.arche.save.rows.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class PersonaTagRow extends SingleStatementRow {
    private final Persona persona;
    private final String key, value;

    public PersonaTagRow(Persona persona, String key, String value) {
        this.persona = persona;
        this.key = key;
        this.value = value;
    }
    
	@Override
	protected String getStatement() {
		return "REPLACE INTO persona_tags(persona_id_fk,tag_key,tag_value) VALUES (?,?,?)";
	}

	@Override
	protected Object getValueFor(int index) {
		switch(index) {
		case 1: return persona.getPersonaId();
		case 2: return key;
		case 3: return value;
		default: throw new IllegalArgumentException();
		}
	}

}
