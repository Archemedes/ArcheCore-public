package net.lordofthecraft.arche.save.archerows.persona.insert;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PersTagRow implements ArcheMergeableRow, ArchePersonaRow {

    final Persona persona;
    final String key, value;
    private Connection connection;

    public PersTagRow(Persona persona, String key, String value) {
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
        return !row.isUnique() && row instanceof PersTagRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        return new MultiPersTagRow(this, (PersTagRow) second);
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
        PreparedStatement statement = connection.prepareStatement("INSERT " + (ArcheCore.usingSQLite() ? "OR IGNORE" : "IGNORE") + " INTO persona_tags(persona_id_fk,tag_key,tag_value) VALUES (?,?,?)");
        statement.setInt(1, persona.getPersonaId());
        statement.setString(2, key);
        statement.setString(3, value);
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public String[] getInserts() {
        return new String[]{"INSERT " + (ArcheCore.usingSQLite() ? "OR IGNORE" : "IGNORE") + " INTO persona_tags(persona_id_fk,tag_key,tag_value)" +
                " VALUES (" + persona.getPersonaId() + ",'" + SQLUtil.mysqlTextEscape(key) + "','" + SQLUtil.mysqlTextEscape(value) + "');"};
    }

    @Override
    public String toString() {
        return "PersTagRow{" +
                "persona=" + MessageUtil.identifyPersona(persona) +
                ", key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
