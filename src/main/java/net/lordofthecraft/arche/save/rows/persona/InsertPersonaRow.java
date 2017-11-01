package net.lordofthecraft.arche.save.rows.persona;

import java.sql.Timestamp;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.StatementRow;

public class InsertPersonaRow extends StatementRow {
    private final Persona persona;
    private final Timestamp now;

    public InsertPersonaRow(Persona persona) {
        this.persona = persona;
        now = now();
    }
    
	@Override
	protected String[] getStatements() {
		return new String[] {
				"INSERT INTO persona(persona_id,player_fk,slot,race,name,gender,last_played) VALUES (?,?,?,?,?,?,?)",
				"INSERT INTO persona_stats(persona_id_fk,renamed,date_created) VALUES (?,?,?)"
		};
	}

	@Override
	protected Object getValueFor(int statement, int index) {
		if(statement == 1) { //The persona statement
			switch(index) {
			case 1: return persona.getPersonaId();
			case 2: return persona.getPlayerUUID();
			case 3: return persona.getSlot();
			case 4: return persona.getRace().name();
			case 5: return persona.getName();
			case 6: return persona.getGender();
			case 7: return now;
			default: throw new IllegalArgumentException();
			}
		} else { //The stats statement
			switch(index) {
			case 1: return persona.getPersonaId();
			case 2: return now;
			case 3: return now;
			default: throw new IllegalArgumentException();
			}
		}
	}

}
