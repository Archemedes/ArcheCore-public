package net.lordofthecraft.arche.save.rows.magic.insert;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.interfaces.Magic;
import net.lordofthecraft.arche.magic.ArcheCreature;
import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class CreatureInsertRow implements ArcheMergeableRow {

    final ArcheCreature creature;
    private Connection connection = null;

    public CreatureInsertRow(ArcheCreature creature) {
        this.creature = creature;
    }

    @Override
    public boolean isUnique() {
        //TODO make merged row
        return true;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !isUnique() && !row.isUnique() && row instanceof CreatureInsertRow;
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
        PreparedStatement statement = connection.prepareStatement("INSERT INTO magic_creatures(id_key,name,descr) VALUES (?,?,?)");
        statement.setString(1, creature.getId());
        statement.setString(2, creature.getName());
        statement.setString(3, creature.getDescription());
        statement.executeUpdate();
        statement.close();

        statement = connection.prepareStatement("INSERT INTO creature_creators(magic_id_fk,creature_fk) VALUES (?,?)");
        for (Magic m : creature.getCreators()) {
            statement.setString(1, m.getName());
            statement.setString(2, creature.getId());
            statement.addBatch();
        }
        statement.executeBatch();
        statement.close();

        statement = connection.prepareStatement("INSERT INTO creature_abilities(creature_fk,ability) VALUES (?,?)");
        for (String ability : creature.getAbilities()) {
            statement.setString(1, creature.getId());
            statement.setString(2, ability);
            statement.addBatch();
        }
        statement.executeBatch();
        statement.close();
    }

    @Override
    public String[] getInserts() {
        ArrayList<String> s = Lists.newArrayList();
        s.add("INSERT INTO magic_creatures(id_key,name,descr) VALUES ('" + creature.getId() + "','" + creature.getName() + "','" + creature.getDescription() + "');");
        for (Magic m : creature.getCreators()) {
            s.add("INSERT INTO creature_creators(magic_id_fk,creature_fk) VALUES ('" + m.getName() + "','" + creature.getId() + "');");
        }
        for (String ability : creature.getAbilities()) {
            s.add("INSERT INTO creature_abilities(creature_fk,ability) VALUES ('" + creature.getId() + "','" + ability + "');");
        }
        return (String[]) s.toArray();
    }
}
