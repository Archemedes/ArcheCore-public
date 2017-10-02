package net.lordofthecraft.arche.save.rows.persona.insert;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.rows.ArchePersonaRow;
import net.lordofthecraft.arche.save.rows.ArcheRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MultiPersTagRow implements ArcheMergeableRow, ArchePersonaRow {

    private final List<PersTagRow> rows = Lists.newArrayList();
    private final List<Persona> personas = Lists.newArrayList();
    private Connection connection = null;

    public MultiPersTagRow(PersTagRow row1, PersTagRow row2) {
        rows.add(row1);
        rows.add(row2);
        personas.addAll(Arrays.asList(row1.getPersonas()));
        personas.addAll(Arrays.asList(row2.getPersonas()));
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return false;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        rows.add((PersTagRow) second);
        personas.addAll(Arrays.asList(((PersTagRow) second).getPersonas()));
        return this;
    }

    @Override
    public Persona[] getPersonas() {
        return (Persona[]) rows.toArray();
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("INSERT " + (ArcheCore.usingSQLite() ? "OR IGNORE" : "IGNORE") + " INTO persona_tags(persona_id_fk,tag_key,tag_value) VALUES (?,?,?)");
            for (PersTagRow row : rows) {
                statement.setInt(1, row.persona.getPersonaId());
                statement.setString(2, row.key);
                statement.setString(3, row.value);
                statement.addBatch();
            }
            statement.executeBatch();

        } catch (SQLException ex) {
            if (statement != null) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[Consumer] Problematic Statement: " + statement.toString());
            }
            throw ex;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[Consumer] Failed to close out PreparedStatement for " + getClass().getSimpleName() + "!", ex);
            }
        }
    }

    @Override
    public String[] getInserts() {
        ArrayList<String> s = Lists.newArrayList();
        for (ArcheRow row : rows) {
            s.addAll(Arrays.asList(row.getInserts()));
        }
        return (String[]) s.toArray();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName() + "[");
        for (ArcheRow row : rows) {
            builder.append(" ").append(row.toString()).append(" ");
        }
        builder.append("]");
        return builder.toString();
    }
}
