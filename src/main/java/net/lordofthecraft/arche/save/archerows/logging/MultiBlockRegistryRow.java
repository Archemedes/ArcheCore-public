package net.lordofthecraft.arche.save.archerows.logging;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArcheRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MultiBlockRegistryRow implements ArcheMergeableRow {

    private final List<BlockRegisteryRow> rows = Lists.newArrayList();
    private Connection connection = null;

    public MultiBlockRegistryRow(BlockRegisteryRow row1, BlockRegisteryRow row2) {
        rows.add(row1);
        rows.add(row2);
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && row instanceof BlockRegisteryRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        rows.add((BlockRegisteryRow) second);
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
            statement = connection.prepareStatement("INSERT " + (ArcheCore.getPlugin().isUsingSQLite() ? "OR IGNORE " : "IGNORE ") + "INTO blockregistry(date,world,x,y,z) VALUES (?,?,?,?)");
            for (BlockRegisteryRow row : rows) {
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statement.setString(2, row.block.getWorld());
                statement.setInt(3, row.block.getX());
                statement.setInt(4, row.block.getY());
                statement.setInt(5, row.block.getZ());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            if (statement != null) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[Consumer] Problematic statement: " + statement.toString());
            }
            throw ex;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[Consumer] Failed to close statement for " + getClass().getSimpleName(), ex);
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
