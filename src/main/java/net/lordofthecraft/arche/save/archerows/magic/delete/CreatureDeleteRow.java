package net.lordofthecraft.arche.save.archerows.magic.delete;

import net.lordofthecraft.arche.magic.ArcheCreature;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CreatureDeleteRow implements ArcheMergeableRow {

    final ArcheCreature creature;
    private Connection connection = null;

    public CreatureDeleteRow(ArcheCreature creature) {
        this.creature = creature;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !isUnique() && !row.isUnique() && row instanceof CreatureDeleteRow;
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
        PreparedStatement statement = connection.prepareStatement("DELETE FROM magic_creatures WHERE id_key=?");
        statement.setString(1, creature.getId());
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public String[] getInserts() {
        return new String[]{
                "DELETE FROM magic_creatures WHERE id_key='" + creature.getId() + "';"
        };
    }
}
