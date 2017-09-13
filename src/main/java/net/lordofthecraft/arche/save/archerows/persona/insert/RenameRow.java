package net.lordofthecraft.arche.save.archerows.persona.insert;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.persona.ArchePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RenameRow implements ArcheMergeableRow, ArchePersonaRow {

    Persona persona;
    final String name;
    private Connection conn;

    public RenameRow(Persona persona, String name) {
        this.persona = persona;
        this.name = name;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && row instanceof RenameRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Can't merge a unique row");
        }
        return new MultiRenameRow(this, (RenameRow) second);
    }

    @Override
    public void setConnection(Connection connection) {
        this.conn = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = conn.prepareStatement("INSERT INTO persona_names (persona_id_fk,name) VALUES (?,?)");
        statement.setInt(1, persona.getPersonaId());
        statement.setString(2, name);
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
        return "RenameRow{" +
                "persona=" + MessageUtil.identifyPersona(persona) +
                ", name='" + name + '\'' +
                '}';
    }
}
