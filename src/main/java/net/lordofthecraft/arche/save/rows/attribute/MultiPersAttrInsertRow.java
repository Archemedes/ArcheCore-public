package net.lordofthecraft.arche.save.rows.attribute;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.rows.ArchePersonaRow;
import net.lordofthecraft.arche.save.rows.ArcheRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MultiPersAttrInsertRow implements ArcheMergeableRow, ArchePersonaRow {

    private final List<PersAttrInsertRow> rows = Lists.newArrayList();
    private final List<Persona> personas = Lists.newArrayList();
    private Connection connection = null;

    public MultiPersAttrInsertRow(PersAttrInsertRow row1, PersAttrInsertRow row2) {
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
        return !row.isUnique() && row instanceof PersAttrInsertRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Unique rows cannot be merged");
        }
        rows.add((PersAttrInsertRow) second);
        personas.addAll(Arrays.asList(((ArchePersonaRow) second).getPersonas()));
        return this;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("INSERT " + (!ArcheCore.getPlugin().isUsingSQLite() ? "IGNORE " : "OR IGNORE ") + " INTO persona_attributes(mod_uuid,persona_id_fk,attribute_type,mod_name,mod_value,operation,created,decayticks,decaytype,lostondeath) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?)");
            for (PersAttrInsertRow row : rows) {
                statement.setString(1, row.mod.getUniqueId().toString());
                statement.setInt(2, row.persona.getPersonaId());
                statement.setString(3, row.attribute.getName());
                statement.setString(4, row.mod.getName());
                statement.setDouble(5, row.mod.getAmount());
                statement.setString(6, row.mod.getOperation().name());
                statement.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
                statement.setLong(8, row.mod.getTicksRemaining());
                statement.setString(9, row.mod.getDecayStrategy().name());
                statement.setBoolean(10, row.mod.isLostOnDeath());
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
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[Consumer] Failed to close statement for MultiPersAttrInsertRow: ", ex);
            }
        }
    }

    @Override
    public Persona[] getPersonas() {
        return (Persona[]) personas.toArray();
    }

    @Override
    public String[] getInserts() {
        List<String> s = Lists.newArrayList();
        for (PersAttrInsertRow row : rows) {
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
