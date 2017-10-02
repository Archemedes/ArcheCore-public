package net.lordofthecraft.arche.save.rows.logging;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;
import net.lordofthecraft.arche.util.WeakBlock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class BlockRegisteryRow implements ArcheMergeableRow {

    final WeakBlock block;
    String data = null;
    private Connection connection;

    public BlockRegisteryRow(WeakBlock block) {
        this.block = block;
    }

    public BlockRegisteryRow(WeakBlock block, String data) {
        this.block = block;
        this.data = data;
    }

    @Override
    public boolean isUnique() {
        return data != null;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !isUnique() && !row.isUnique() && row instanceof BlockRegisteryRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        return new MultiBlockRegistryRow(this, (BlockRegisteryRow) second);
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        if (isUnique()) {
            PreparedStatement statement = connection.prepareStatement("INSERT " + (ArcheCore.getPlugin().isUsingSQLite() ? "OR IGNORE " : "IGNORE ") + "INTO blockregistry(date,world,x,y,z,data) VALUES (?,?,?,?,?)");
            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            statement.setString(2, block.getWorld());
            statement.setInt(3, block.getX());
            statement.setInt(4, block.getY());
            statement.setInt(5, block.getZ());
            statement.setString(6, data);
            statement.executeUpdate();
            statement.close();
        } else {
            PreparedStatement statement = connection.prepareStatement("INSERT " + (ArcheCore.getPlugin().isUsingSQLite() ? "OR IGNORE " : "IGNORE ") + "INTO blockregistry(date,world,x,y,z) VALUES (?,?,?,?)");
            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            statement.setString(2, block.getWorld());
            statement.setInt(3, block.getX());
            statement.setInt(4, block.getY());
            statement.setInt(5, block.getZ());
            statement.executeUpdate();
            statement.close();
        }
    }

    @Override
    public String[] getInserts() {
        if (isUnique()) {
            return new String[]{
                    "INSERT " + (ArcheCore.getPlugin().isUsingSQLite() ? "OR IGNORE " : "IGNORE ") + "INTO blockregistry(date,world,x,y,z,data) VALUES (FROM_UNIXTIME(" + System.currentTimeMillis() + "),'" + block.getWorld() + "'," + block.getX() + "," + block.getY() + "," + block.getZ() + ",'" + data + "');"
            };
        }
        return new String[]{
                "INSERT " + (ArcheCore.getPlugin().isUsingSQLite() ? "OR IGNORE " : "IGNORE ") + "INTO blockregistry(world,x,y,z) VALUES ('" + block.getWorld() + "'," + block.getX() + "," + block.getY() + "," + block.getZ() + ");"
        };
    }

    @Override
    public String toString() {
        return "BlockRegisteryRow{" +
                "block=" + block +
                ", data='" + data + '\'' +
                '}';
    }
}
