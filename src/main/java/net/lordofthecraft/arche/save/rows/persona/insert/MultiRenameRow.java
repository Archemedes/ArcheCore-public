package net.lordofthecraft.arche.save.rows.persona.insert;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.rows.ArchePersonaRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MultiRenameRow implements ArcheMergeableRow, ArchePersonaRow {

    final List<RenameRow> rows = Lists.newArrayList();
    final List<OfflinePersona> personas = Lists.newArrayList();
    private Connection conn;

    public MultiRenameRow(RenameRow row1, RenameRow row2) {
        rows.add(row1);
        rows.add(row2);
        personas.addAll(Lists.newArrayList(row1.getPersonas()));
        personas.addAll(Lists.newArrayList(row2.getPersonas()));
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && row instanceof RenameRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        rows.add((RenameRow) second);
        personas.addAll(Arrays.asList(((ArchePersonaRow) second).getPersonas()));
        return this;
    }

    @Override
    public void setConnection(Connection connection) {
        this.conn = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement("INSERT INTO persona_names (persona_id_fk,name) VALUES (?,?)");
            for (RenameRow row : rows) {
                statement.setInt(1, row.persona.getPersonaId());
                statement.setString(2, row.name);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            if (statement != null) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[Consumer] Problematic MultiRenameRow statement: " + statement.toString());
            }
            throw ex;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[Consumer] We failed to close the statement in MultiRenameRow!", ex);
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
        for (RenameRow row : rows) {
            s.addAll(Arrays.asList(row.getInserts()));
        }
        return (String[]) s.toArray();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(getClass().getSimpleName() + "[");
        for (RenameRow row : rows) {
            builder.append(" ").append(row.toString()).append(" ");
        }
        builder.append("]");
        return builder.toString();
    }
}
