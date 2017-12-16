package net.lordofthecraft.arche.save.rows.persona;

import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class NamelogRow extends SingleStatementRow {
	private final int personaId;
	private final String newName;
	
	public NamelogRow(int personaId, String newName) {
		this.personaId = personaId;
		this.newName = newName;
	}
	
	@Override
	protected String getStatement() {
		return "REPLACE INTO persona_name(persona_id_fk,name) VALUES (?,?)";
	}

	@Override
	protected Object getValueFor(int index) {
		switch (index) {
		case 1: return personaId;
		case 2: return newName;
		default: throw new IllegalArgumentException();
		}
	}

}
