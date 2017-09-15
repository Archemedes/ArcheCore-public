package net.lordofthecraft.arche.save.archerows.logging;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.save.archerows.ArcheRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MultiEconomyLogRow implements ArcheMergeableRow, ArchePersonaRow {

    private final List<EconomyLogRow> rows = Lists.newArrayList();
    private final ArrayList<Persona> personas = Lists.newArrayList();
    private Connection connection = null;

    public MultiEconomyLogRow(EconomyLogRow row1, EconomyLogRow row2) {
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
        return !row.isUnique() && row instanceof EconomyLogRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        rows.add((EconomyLogRow) second);
        personas.addAll(Arrays.asList(((EconomyLogRow) second).getPersonas()));
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
            statement = connection.prepareStatement("INSERT INTO econ_log(date,persona_id_fk,type,amount,plugin,reason,amt_before,amt_after) VALUES (?,?,?,?,?,?,?,?)");
            for (EconomyLogRow row : rows) {
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statement.setInt(2, row.persona.getPersonaId());
                statement.setString(3, row.type.name());
                statement.setDouble(4, row.amount);
                statement.setString(5, row.transaction.getRegisteringPluginName());
                statement.setString(6, row.transaction.getCause());
                statement.setDouble(7, row.before);
                statement.setDouble(8, row.after);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            if (statement != null) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] Problematic statement: " + statement.toString());
            }
            throw ex;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] Failed to close out PreparedStatement for MultiEconomyLogRow", ex);
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
