package net.lordofthecraft.arche.save.rows.logging;

import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;
import net.lordofthecraft.arche.util.SQLUtil;
import net.lordofthecraft.arche.util.WeakBlock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DelBlockRegistryRow implements ArcheMergeableRow {

    final WeakBlock wb;
    private Connection connection = null;

    public DelBlockRegistryRow(WeakBlock wb) {
        this.wb = wb;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && row instanceof DelBlockRegistryRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        return new MultiDelBlockRegistryRow(this, (DelBlockRegistryRow) second);
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM blockregistry WHERE world=? AND x=? AND y=? AND z=?");
        statement.setString(1, wb.getWorld());
        statement.setInt(2, wb.getX());
        statement.setInt(3, wb.getY());
        statement.setInt(4, wb.getZ());
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public String[] getInserts() {
        return new String[]{
                "DELETE FROM blockregistry WHERE world='" + SQLUtil.mysqlTextEscape(wb.getWorld()) + "' AND x=" + wb.getX() + " AND y=" + wb.getY() + " AND z=" + wb.getZ() + ";"
        };
    }

    @Override
    public String toString() {
        return "DelBlockRegistryRow{" +
                "wb=" + wb +
                '}';
    }
}
