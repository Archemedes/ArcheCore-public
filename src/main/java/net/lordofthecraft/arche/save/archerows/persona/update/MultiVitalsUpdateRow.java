package net.lordofthecraft.arche.save.archerows.persona.update;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.save.archerows.ArcheRow;
import net.lordofthecraft.arche.save.tasks.persona.UpdateVitalsTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MultiVitalsUpdateRow implements ArcheMergeableRow, ArchePersonaRow {

    private final List<VitalsUpdateRow> rows = Lists.newArrayList();
    private final List<Persona> personas = Lists.newArrayList();
    private Connection connection = null;

    public MultiVitalsUpdateRow(VitalsUpdateRow row1, VitalsUpdateRow row2) {
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
        return !row.isUnique() && row instanceof UpdateVitalsTask;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (!second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge a unique row");
        }
        rows.add((VitalsUpdateRow) second);
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
            statement = connection.prepareStatement("UPDATE persona_vitals SET world=? AND x=? AND y=? AND z=? AND health=? AND saturation=? AND hunger=? AND inv=? AND ender_inv=? AND potions=? WHERE persona_id_fk=?");
            for (VitalsUpdateRow row : rows) {
                if (!row.persona.isDeleted()) {
                    statement.setString(1, row.world.toString());
                    statement.setInt(2, row.x);
                    statement.setInt(3, row.y);
                    statement.setInt(4, row.z);
                    statement.setDouble(5, row.health);
                    statement.setFloat(6, row.saturation);
                    statement.setInt(7, row.hunger);
                    statement.setString(8, row.inv.getInvAsString());
                    statement.setString(9, row.inv.getEnderInvAsString());
                    statement.setString(10, row.potions);
                    statement.setInt(11, row.persona.getPersonaId());
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            if (statement != null) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] Problematic Statement: " + statement.toString());
            }
            throw ex;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] Failed to close PreparedStatement in MultiVitalsUpdateRow! ", ex);
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
        for (VitalsUpdateRow row : rows) {
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
