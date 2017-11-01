package net.lordofthecraft.arche.save.rows.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class PersonaSkinRow extends SingleStatementRow {
    private final int persona_id;
    private final int skin_id;

    public PersonaSkinRow(Persona persona) {
        persona_id = persona.getPersonaId();
        skin_id = persona.getSkin().getSkinId();
    }
    
	@Override
	protected String getStatement() {
		return "INSERT INTO per_persona_skins(persona_id_fk,skin_id_fk) VALUES (?,?)";
	}

	@Override
	protected Object getValueFor(int index) {
		switch(index) {
		case 1: return persona_id;
		case 2: return skin_id;
		default: throw new IllegalArgumentException();
		}
	}

}
