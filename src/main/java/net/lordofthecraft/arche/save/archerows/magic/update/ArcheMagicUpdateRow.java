package net.lordofthecraft.arche.save.archerows.magic.update;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArcheMagicUpdateRow implements ArcheMergeableRow {

    final ArcheMagic magic;
    final ArcheMagic.Field field;
    final Object data;
    private Connection connection = null;

    public ArcheMagicUpdateRow(ArcheMagic magic, ArcheMagic.Field field, Object data) {
        this.magic = magic;
        this.field = field;
        this.data = data;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && !isUnique() && row instanceof ArcheMagicUpdateRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        return null;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE magics SET ?=? WHERE id_key=?");
        statement.setString(1, field.field);
        if (ArcheCore.getPlugin().isUsingSQLite()) {
            switch (field) {
                case MAX_TIER:
                case DAYS_TO_MAX:
                case DAYS_TO_EXTRA:
                    statement.setInt(1, (int) data);
                    break;
                case EXTRA_TIER:
                case SELF_TEACH:
                case TEACHABLE:
                    statement.setBoolean(1, (boolean) data);
                    break;
                case DESCRIPTION:
                case LABEL:
                case ARCHETYPE:
                    statement.setString(1, (String) data);
                    break;
            }
        } else {
            statement.setObject(1, data, field.type);
        }
        statement.setString(3, magic.getName());
    }

    @Override
    public String[] getInserts() {
        return new String[]{
                "UPDATE magics SET " + field.field + "=" + (field.type == JDBCType.VARCHAR ? "'" + data + "'" : data) + " WHERE id_key='" + magic.getName() + "';"
        };
    }
}
