package net.lordofthecraft.arche.save.rows.persona;

import lombok.RequiredArgsConstructor;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

@RequiredArgsConstructor
public class PruneSessionsRow extends SingleStatementRow {
	private final int personaId;

	@Override
	protected String getStatement() {
		return "DELETE FROM account_sessions WHERE time_played<3 AND persona_id_fk=?";
	}

	@Override
	protected Object getValueFor(int index) {
		return personaId;
	}

}
