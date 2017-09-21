package net.lordofthecraft.arche.save.archerows.persona.update;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdatePersTagRow implements ArcheMergeableRow, ArchePersonaRow {

    final Persona persona;
    final String key, value;
    private Connection connection = null;

    public UpdatePersTagRow(Persona persona, String key, String value) {
        this.persona = persona;
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && row instanceof UpdatePersTagRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        return new MultiUpdatePersTagRow(this, (UpdatePersTagRow) second);
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
        PreparedStatement statement = connection.prepareStatement("UPDATE persona_tags SET tag_value=? WHERE persona_id_fk=? AND tag_key=?");
        statement.setString(1, value);
        statement.setInt(2, persona.getPersonaId());
        statement.setString(3, key);
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public String[] getInserts() {
        return new String[]{"UPDATE persona_tags SET tag_value='" + SQLUtil.mysqlTextEscape(value) + "' WHERE persona_id_fk=" + persona.getPersonaId() + " AND tag_key='" + SQLUtil.mysqlTextEscape(key) + "';"};
    }

    @Override
    public String toString() {
        return "UpdatePersTagRow{" +
                "persona=" + MessageUtil.identifyPersona(persona) +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
