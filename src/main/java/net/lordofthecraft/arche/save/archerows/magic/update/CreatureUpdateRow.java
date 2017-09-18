package net.lordofthecraft.arche.save.archerows.magic.update;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.magic.ArcheCreature;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreatureUpdateRow implements ArcheMergeableRow {

    final ArcheCreature creature;
    final ArcheCreature.Field field;
    final Object data;
    private Connection connection = null;

    public CreatureUpdateRow(ArcheCreature creature, ArcheCreature.Field field, Object data) {
        this.creature = creature;
        this.field = field;
        this.data = data;
    }

    @Override
    public boolean isUnique() {
        //TODO make merged row
        return true;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !isUnique() && !row.isUnique() && (row instanceof CreatureUpdateRow && ((CreatureUpdateRow) row).field.table.equalsIgnoreCase(field.table));
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
        if (field.insert) {
            PreparedStatement statement = connection.prepareStatement("INSERT " + (ArcheCore.getPlugin().isUsingSQLite() ? "OR IGNORE " : "IGNORE ") + " INTO " + field.table + "(creature_fk,?) VALUES (?,?)");
            statement.setString(1, field.field);
            statement.setString(2, creature.getId());
            if (ArcheCore.getPlugin().isUsingSQLite()) {
                switch (field) {
                    case NAME:
                    case DESCRIPTION:
                    case ABILITY:
                    case CREATOR:
                        statement.setString(3, (String) data);
                }
            } else {
                statement.setObject(3, data, field.type);
            }
            statement.executeUpdate();
            statement.close();
        } else {
            PreparedStatement statement = connection.prepareStatement("UPDATE magic_creatures SET " + field.field + "=? WHERE id_key=?");
            if (ArcheCore.getPlugin().isUsingSQLite()) {
                switch (field) {
                    case NAME:
                    case DESCRIPTION:
                    case ABILITY:
                    case CREATOR:
                        statement.setString(1, (String) data);
                }
            } else {
                statement.setObject(1, data, field.type);
            }
            statement.setString(2, creature.getId());
            statement.executeUpdate();
            statement.close();
        }
    }

    @Override
    public String[] getInserts() {
        if (field.insert) {
            return new String[]{"INSERT " + (ArcheCore.getPlugin().isUsingSQLite() ? "OR IGNORE " : "IGNORE ") + " INTO " + field.table + "(creature_fk," + field.field + ") " +
                    "VALUES ('" + SQLUtil.mysqlTextEscape(creature.getId()) + "'," + (field.type == JDBCType.VARCHAR ? "'" + data + "'" : data) + ");"};
        }
        return new String[]{
                "UPDATE magic_creatures SET " + field.field + "=" + (field.type == JDBCType.VARCHAR ? "'" + data + "'" : data) + " WHERE id_key='" + creature.getId() + "';"
        };
    }
}
