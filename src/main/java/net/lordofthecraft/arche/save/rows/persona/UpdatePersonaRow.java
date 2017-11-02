package net.lordofthecraft.arche.save.rows.persona;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import org.jsoup.helper.Validate;

import com.google.common.collect.Maps;

import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.PersonaTable;
import net.lordofthecraft.arche.save.rows.StatementRow;

public final class UpdatePersonaRow extends StatementRow {
	Map<PersonaField, PreparedStatement> cached = Maps.newEnumMap(PersonaField.class);
	
	private final int persona;
	private final PersonaField field;
	private final Object value;
	
	public UpdatePersonaRow(OfflinePersona persona, PersonaField field, Object value) {
		this.persona = persona.getPersonaId();
		this.field = field;
		this.value = value;
	}

	@Override
	public boolean isUnique() {
		return true; //Never merge this please
	}

	@Override
	public PreparedStatement[] prepare(Connection connection) throws SQLException {
		PreparedStatement result = cached.get(field);
		if(result == null || result.isClosed()) {
			result = connection.prepareStatement(getStatement());
			cached.put(field, result);
			statPool.add(result);
		}
		
		return new PreparedStatement[] {result};
	}

	@Override
	protected String[] getStatements() {
		return new String[] {getStatement()};
	}

	@Override
	protected Object getValueFor(int statement, int varIndex) {
		Validate.isTrue(statement == 1 && (varIndex == 1 || varIndex == 2));
		return varIndex == 1? value : persona;
	}
	
	private String getStatement() {
		return "UPDATE " + field.table.getTable() 
		+ " SET " + field.field() + "=? WHERE persona_id" + (field.table == PersonaTable.MASTER ? "=" : "_fk=") + "?";
	}

}
