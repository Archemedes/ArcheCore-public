package net.lordofthecraft.arche.save.rows.persona;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class PersonaRenameRow extends SingleStatementRow {
    private final Persona persona;
    private final String name;

    public PersonaRenameRow(Persona persona, String name) {
        this.persona = persona;
        this.name = name;
    }


    @Override
    protected String getStatement() {
        return "INSERT INTO persona_names (persona_id_fk,name) VALUES (?,?)";
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1:
                return persona.getPersonaId();
            case 2:
                return name;
            default:
                throw new IllegalArgumentException();
        }
    }

}
