package net.lordofthecraft.arche.save.archerows.persona.attribute;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MultiPersAttrRemoveRow implements ArcheMergeableRow {

    private final List<PersAttrRemoveRow> rows = Lists.newArrayList();
    private final List<Persona> personas = Lists.newArrayList();
    private Connection connection = null;

    public MultiPersAttrRemoveRow(PersAttrRemoveRow row1, PersAttrRemoveRow row2) {
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
        return !row.isUnique() && row instanceof PersAttrRemoveRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        rows.add((PersAttrRemoveRow) second);
        personas.addAll(Arrays.asList(second.getPersonas()));
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
            statement = connection.prepareStatement("DELETE FROM persona_attributes WHERE moduuid=? AND persona_id_fk=? AND attribute_type=?");
            for (PersAttrRemoveRow row : rows) {
                statement.setString(1, row.mod.getUniqueId().toString());
                statement.setInt(2, row.mod.getPersonaId());
                statement.setString(3, row.mod.getAttribute().getName());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            if (statement != null) {
                ArcheCore.getPlugin().getLogger().info("[ArcheCore Consumer] Problematic statement: " + statement.toString());
            }
            throw ex;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] Failed to close statement for ");
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
}
