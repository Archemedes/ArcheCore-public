package net.lordofthecraft.arche.save.archerows.magic.delete;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.save.archerows.ArcheRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MultiMagicDeleteRow implements ArcheMergeableRow, ArchePersonaRow {

    private final List<MagicDeleteRow> rows = Lists.newArrayList();
    private final ArrayList<Persona> personas = Lists.newArrayList();
    private Connection connection = null;

    public MultiMagicDeleteRow(MagicDeleteRow row1, MagicDeleteRow row2) {
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
        return !row.isUnique() && row instanceof MagicDeleteRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        rows.add((MagicDeleteRow) second);
        personas.addAll(Arrays.asList(((MagicDeleteRow) second).getPersonas()));
        return this;
    }

    @Override
    public Persona[] getPersonas() {
        return (Persona[]) personas.toArray();
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("DELETE FROM persona_magic WHERE persona_id_fk=? AND magic_fk=?");
            for (MagicDeleteRow row : rows) {
                statement.setInt(1, row.persona.getPersonaId());
                statement.setString(2, row.magic.getName());
                statement.addBatch();
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
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] Failed to close out PreparedStatement in " + getClass().getSimpleName() + "!", ex);
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
}
