package net.lordofthecraft.arche.save.rows.persona.delete;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.rows.ArchePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DelPersTagRow implements ArcheMergeableRow, ArchePersonaRow {

    final Persona persona;
    final String key;
    private Connection connection;

    public DelPersTagRow(Persona persona, String key) {
        this.persona = persona;
        this.key = key;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && row instanceof DelPersTagRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        return new MultiDelPersTagRow(this, (DelPersTagRow) second);
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
        PreparedStatement statement = connection.prepareStatement("DELETE FROM persona_tags WHERE persona_id_fk=? AND tag_key=?");
        statement.setInt(1, persona.getPersonaId());
        statement.setString(2, key);
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public String[] getInserts() {
        return new String[]{"DELETE FROM persona_tags WHERE persona_id_fk=" + persona.getPersonaId() + " AND tag_key='" + SQLUtil.mysqlTextEscape(key) + "';"};
    }

    @Override
    public String toString() {
        return "DelPersTagRow{" +
                "persona=" + MessageUtil.identifyPersona(persona) +
                ", key='" + key + '\'' +
                '}';
    }
}
