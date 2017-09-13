package net.lordofthecraft.arche.save.archerows.persona.delete;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MultiPersonaDeleteRow implements ArcheMergeableRow {

    final List<PersonaDeleteRow> rows = Lists.newArrayList();
    final List<Persona> personas = Lists.newArrayList();
    private Connection conn = null;

    public MultiPersonaDeleteRow(PersonaDeleteRow row1, PersonaDeleteRow row2) {
        rows.add(row1);
        rows.add(row2);
        personas.addAll(Arrays.asList(row1.getPersonas()));
        personas.addAll(Arrays.asList(row2.getPersonas()));
    }

    @Override
    public boolean isUnique() {
        return true;
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
        rows.add((PersonaDeleteRow) second);
        personas.addAll(Arrays.asList(second.getPersonas()));
        return this;
    }

    @Override
    public void setConnection(Connection connection) {
        this.conn = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        CallableStatement statement = null;
        try {
            statement = conn.prepareCall("{call persona_delete(?)}");
            for (PersonaDeleteRow row : rows) {
                if (!row.persona.isDeleted()) {
                    statement.setInt(1, row.persona.getPersonaId());
                    statement.addBatch();
                    ((ArchePersona) row.persona).setDeleted(true);
                }
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            if (statement != null) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] Problematic statement: " + statement);
            }
            throw ex;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] We failed to close a CallableStatement in MultiPersonaDeleteRow!", ex);
            }
        }
    }

    @Override
    public Persona[] getPersonas() {
        return (Persona[]) personas.toArray();
    }

    @Override
    public String[] getInserts() {
        return new String[0];
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName() + "[");
        for (PersonaDeleteRow row : rows) {
            builder.append(" ").append(row.toString()).append(" ");
        }
        builder.append("]");
        return builder.toString();
    }
}
