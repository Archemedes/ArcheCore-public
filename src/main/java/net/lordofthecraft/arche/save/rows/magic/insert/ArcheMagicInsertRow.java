package net.lordofthecraft.arche.save.rows.magic.insert;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.interfaces.Magic;
import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

public class ArcheMagicInsertRow implements ArcheMergeableRow {

    final ArcheMagic magic;
    private Connection connection = null;

    public ArcheMagicInsertRow(ArcheMagic magic) {
        this.magic = magic;
    }

    @Override
    public boolean isUnique() {
        //TODO make merged row
        return true;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !isUnique() && !row.isUnique() && row instanceof ArcheMagicInsertRow;
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
        PreparedStatement statement = connection.prepareStatement("INSERT INTO magics (id_key,max_tier,self_teach,teachable,description,label,days_to_max,days_to_extra,archetype) " +
                "VALUES (?,?,?,?,?,?,?,?,?)");
        statement.setString(1, magic.getName());
        statement.setInt(2, magic.getMaxTier());
        statement.setBoolean(3, magic.isSelfTeachable());
        statement.setBoolean(4, magic.isTeachable());
        statement.setString(5, magic.getDescription());
        statement.setString(6, magic.getLabel());
        statement.setInt(7, magic.getDaysToMaxTier());
        statement.setInt(8, magic.getDaysToBonusTier());
        statement.setString(9, magic.getType().getKey());
        statement.executeUpdate();
        statement.close();

        if (magic.getWeaknesses().size() > 0) {
            statement = connection.prepareStatement("INSERT INTO magic_weaknesses(fk_source_magic,fk_weakness_magic,modifier) VALUES (?,?,?)");
            for (Map.Entry<Magic, Double> ent : magic.getWeaknesses().entrySet()) {
                statement.setString(1, magic.getName());
                statement.setString(2, ent.getKey().getName());
                statement.setDouble(3, ent.getValue());
                statement.addBatch();
            }
            statement.executeBatch();
            statement.close();
        }
    }

    @Override
    public String[] getInserts() {
        ArrayList<String> s = Lists.newArrayList();
        s.add("INSERT INTO magics (id_key,max_tier,self_teach,teachable,description,label,days_to_max,days_to_extra,archetype) " +
                "VALUES ('" + SQLUtil.mysqlTextEscape(magic.getName())
                + "'," + magic.getMaxTier()
                + "," + magic.isSelfTeachable()
                + "," + magic.isTeachable()
                + ",'" + SQLUtil.mysqlTextEscape(magic.getDescription())
                + "','" + SQLUtil.mysqlTextEscape(magic.getLabel())
                + "'," + magic.getDaysToMaxTier()
                + "," + magic.getDaysToBonusTier()
                + ",'" + SQLUtil.mysqlTextEscape(magic.getType().getKey()) + "');");
        if (magic.getWeaknesses().size() > 0) {
            for (Map.Entry<Magic, Double> ent : magic.getWeaknesses().entrySet()) {
                s.add("INSERT INTO magic_weaknesses(fk_source_magic,fk_weakness_magic,modifier) VALUES ('" + magic.getName() + "','" + ent.getKey().getName() + "'," + ent.getValue() + ")");
            }
        }
        return (String[]) s.toArray();
    }
}
