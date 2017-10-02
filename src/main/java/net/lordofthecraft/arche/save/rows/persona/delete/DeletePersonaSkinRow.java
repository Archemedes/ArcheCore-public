package net.lordofthecraft.arche.save.rows.persona.delete;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.ArchePersonaRow;
import net.lordofthecraft.arche.save.rows.ArchePreparedStatementRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeletePersonaSkinRow implements ArchePreparedStatementRow, ArchePersonaRow {

    private final Persona persona;
    private Connection connection = null;

    public DeletePersonaSkinRow(Persona persona) {
        this.persona = persona;
    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{persona};
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM per_persona_skins WHERE persona_id_fk=?");
        statement.setInt(1, persona.getPersonaId());
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public String[] getInserts() {
        return new String[]{"DELETE FROM per_persona_skins WHERE persona_id_fk=" + persona.getPersonaId() + ";"};
    }
}
