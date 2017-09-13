package net.lordofthecraft.arche.save.archerows.persona.delete;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.util.MessageUtil;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

public class PersonaDeleteRow implements ArcheMergeableRow {

    final Persona persona;
    private Connection conn = null;

    public PersonaDeleteRow(Persona persona) {
        this.persona = persona;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && row instanceof PersonaDeleteRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Can't merge a unique row");
        }
        return new MultiPersonaDeleteRow(this, (PersonaDeleteRow) second);
    }

    @Override
    public void setConnection(Connection connection) {
        this.conn = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        if (persona.isDeleted()) return;
        CallableStatement statement = conn.prepareCall("{call persona_delete(?)}");
        statement.setInt(1, persona.getPersonaId());
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{persona};
    }

    @Override
    public String[] getInserts() {
        return new String[0];
    }

    @Override
    public String toString() {
        return "PersonaDeleteRow{" +
                "persona=" + MessageUtil.identifyPersona(persona) +
                '}';
    }
}
