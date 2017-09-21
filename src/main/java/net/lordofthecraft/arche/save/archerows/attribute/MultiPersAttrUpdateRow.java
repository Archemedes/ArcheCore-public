package net.lordofthecraft.arche.save.archerows.attribute;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.save.archerows.ArcheRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MultiPersAttrUpdateRow implements ArcheMergeableRow, ArchePersonaRow {

    private final List<PersAttrUpdateRow> rows = Lists.newArrayList();
    private final List<Persona> personas = Lists.newArrayList();
    private Connection connection = null;

    public MultiPersAttrUpdateRow(PersAttrUpdateRow row1, PersAttrUpdateRow row2) {
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
        return !row.isUnique() && row instanceof PersAttrUpdateRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        rows.add((PersAttrUpdateRow) second);
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
            statement = connection.prepareStatement("UPDATE persona_attributes SET decaytype=? AND decaytime=? AND lostondeath=? WHERE persona_id_fk=? AND mod_uuid=? AND attribute_type=?");
            for (PersAttrUpdateRow row : rows) {
                statement.setString(1, row.mod.getDecayStrategy().name());
                statement.setLong(2, row.mod.getTicksRemaining());
                statement.setBoolean(3, row.mod.isLostOnDeath());
                statement.setInt(4, row.persona.getPersonaId());
                statement.setString(5, row.mod.getUniqueId().toString());
                statement.setString(6, row.attribute.getName());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            if (statement != null) {
                ArcheCore.getPlugin().getLogger().log(Level.INFO, "[Consumer] Problematic Statement: " + statement.toString());
            }
            throw ex;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[Consumer] Failed to close statement for MultiPersAttrUpdateRow!: ", ex);
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
        for (PersAttrUpdateRow row : rows) {
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
