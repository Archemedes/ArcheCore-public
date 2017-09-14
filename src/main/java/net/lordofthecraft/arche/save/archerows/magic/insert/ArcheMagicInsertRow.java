package net.lordofthecraft.arche.save.archerows.magic.insert;

import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArcheMagicInsertRow implements ArcheMergeableRow {

    final ArcheMagic magic;
    private Connection connection = null;

    public ArcheMagicInsertRow(ArcheMagic magic) {
        this.magic = magic;
    }

    @Override
    public boolean isUnique() {
        return false;
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

    /*
            statement.execute("CREATE TABLE IF NOT EXISTS magics (" +
                "id_key VARCHAR(255)," +
                "max_tier INT DEFAULT 5," +
                "extra_tier BOOLEAN DEFAULT FALSE," +
                "self_teach BOOLEAN DEFAULT FALSE," +
                "teachable BOOLEAN DEFAULT TRUE," +
                "description TEXT DEFAULT NULL," +
                "label TEXT NOT NULL," +
                "days_to_max INT UNSIGNED DEFAULT 120," +
                "days_to_extra INT UNSIGNED DEFAULT 0," +
                "archetype VARCHAR(255) NOT NULL," +
                "PRIMARY KEY (id_key)," +
                "FOREIGN KEY (archetype) REFERENCES magic_archetypes (id_key) ON UPDATE CASCADE ON DELETE RESTRICT" +
                ")" +
                end);
     */

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
    }

    @Override
    public String[] getInserts() {
        return new String[]{
                "INSERT INTO magics (id_key,max_tier,self_teach,teachable,description,label,days_to_max,days_to_extra,archetype) " +
                        "VALUES ('" + SQLUtil.mysqlTextEscape(magic.getName())
                        + "'," + magic.getMaxTier()
                        + "," + magic.isSelfTeachable()
                        + "," + magic.isTeachable()
                        + ",'" + SQLUtil.mysqlTextEscape(magic.getDescription())
                        + "','" + SQLUtil.mysqlTextEscape(magic.getLabel())
                        + "'," + magic.getDaysToMaxTier()
                        + "," + magic.getDaysToBonusTier()
                        + ",'" + SQLUtil.mysqlTextEscape(magic.getType().getKey()) + "');"
        };
    }
}
