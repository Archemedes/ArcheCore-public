package net.lordofthecraft.arche.save.rows.magic.delete;

import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArcheMagicDeleteRow implements ArcheMergeableRow {

    final ArcheMagic magic;
    private Connection connection;

    public ArcheMagicDeleteRow(ArcheMagic magic) {
        this.magic = magic;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !isUnique() && !row.isUnique() && row instanceof ArcheMagicDeleteRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        return new MultiArcheMagicDeleteRow(this, (ArcheMagicDeleteRow) second);
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("DELETE FROM magics WHERE id_key=?");
        statement.setString(1, magic.getName());
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public String[] getInserts() {
        return new String[]{
                "DELETE FROM magics WHERE id_key='" + magic.getName() + "';"
        };
    }

    @Override
    public String toString() {
        return "ArcheMagicDeleteRow{" +
                "magic=" + magic +
                '}';
    }
}
