package net.lordofthecraft.arche.save.archerows.persona.insert;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.save.archerows.ArchePreparedStatementRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SkinRow implements ArchePreparedStatementRow, ArchePersonaRow {

    private final Persona persona;
    private Connection connection = null;

    public SkinRow(Persona persona) {
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
        PreparedStatement statement = connection.prepareStatement("INSERT INTO per_persona_skins(persona_id_fk,skin_id_fk) VALUES (?,?)");
        statement.setInt(1, persona.getPersonaId());
        statement.setInt(2, persona.getSkin().getSkinId());
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public String[] getInserts() {
        return new String[]{"INSERT INTO per_persona_skins(persona_id_fk,skin_id_fk) VALUES (" + persona.getPersonaId() + "," + persona.getSkin().getSkinId() + ");"};
    }
}
