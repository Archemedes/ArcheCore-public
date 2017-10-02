package net.lordofthecraft.arche.save.rows.magic.insert;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.magic.ArcheMagic;
import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.rows.ArchePersonaRow;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MagicInsertRow implements ArcheMergeableRow, ArchePersonaRow {

    final Persona persona;
    final ArcheMagic magic;
    final int tier;
    final Integer teacher;
    final boolean visible;
    private Connection connection = null;

    public MagicInsertRow(Persona persona, ArcheMagic magic, int tier, Integer teacher, boolean visible) {
        this.persona = persona;
        this.magic = magic;
        this.tier = tier;
        this.teacher = teacher;
        this.visible = visible;
    }

    @Override
    public boolean isUnique() {
        //TODO make merged row
        return true;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !isUnique() && !row.isUnique() && row instanceof MagicInsertRow;
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
        PreparedStatement statement = connection.prepareStatement("INSERT INTO persona_magics(magic_fk,persona_id_fk,tier,last_advanced,teacher,learned,visible) VALUES (?,?,?,?,?,?,?)");
        statement.setString(1, magic.getName());
        statement.setInt(2, persona.getPersonaId());
        statement.setInt(3, tier);
        statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
        statement.setInt(5, teacher);
        statement.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
        statement.setBoolean(7, visible);
        statement.executeUpdate();
        statement.close();


    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{persona};
    }

    @Override
    public String[] getInserts() {
        return new String[]{
                "INSERT INTO persona_magics(magic_fk,persona_id_fk,tier,last_advanced,teacher,learned,visible) VALUES ('" + SQLUtil.mysqlTextEscape(magic.getName())
                        + "'," + persona.getPersonaId()
                        + "," + tier
                        + ",FROM_UNIXTIME(" + System.currentTimeMillis() + ")"
                        + "," + teacher
                        + ",FROM_UNIXTIME(" + System.currentTimeMillis() + ")"
                        + "," + visible + ");"
        };
    }
}
