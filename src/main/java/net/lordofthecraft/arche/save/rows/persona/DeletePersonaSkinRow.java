package net.lordofthecraft.arche.save.rows.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class DeletePersonaSkinRow extends SingleStatementRow {
    private final Persona persona;

    public DeletePersonaSkinRow(Persona persona) {
        this.persona = persona;
    }
	@Override
	protected String getStatement() {
		return "DELETE FROM per_persona_skins WHERE persona_id_fk=?";
	}

	@Override
	protected Object getValueFor(int index) {
		return persona.getPersonaId();
	}

}
