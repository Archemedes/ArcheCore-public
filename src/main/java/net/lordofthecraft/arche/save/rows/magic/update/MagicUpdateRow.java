package net.lordofthecraft.arche.save.rows.magic.update;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.MagicAttachment;
import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.rows.ArchePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;

import java.sql.*;

public class MagicUpdateRow implements ArcheMergeableRow, ArchePersonaRow {

    final Persona persona;
    final String magic_name;
    final MagicAttachment.Field field;
    final Object toSet;
    private Connection connection = null;

    public MagicUpdateRow(Persona persona, String magic_name, MagicAttachment.Field field, Object toSet) {
        this.persona = persona;
        this.magic_name = magic_name;
        this.field = field;
        this.toSet = toSet;
    }

    @Override
    public boolean isUnique() {
        //TODO make merged row
        return true;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !isUnique() && !row.isUnique() && row instanceof MagicUpdateRow;
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
        PreparedStatement statement = connection.prepareStatement("UPDATE persona_magic SET ?=? WHERE persona_id_fk=? AND magic_fk=?");
        statement.setString(1, field.field);
        if (ArcheCore.getPlugin().isUsingSQLite()) {
            switch (field) {
                case TIER:
                case TEACHER:
                    statement.setInt(2, (int) toSet);
                    break;
                case LAST_ADVANCED:
                case LEARNED:
                    statement.setTimestamp(2, (Timestamp) toSet);
                    break;
                case VISIBLE:
                    statement.setBoolean(2, (boolean) toSet);
                    break;
            }
        } else {
            statement.setObject(2, toSet, field.type);
        }
        statement.setInt(3, persona.getPersonaId());
        statement.setString(4, magic_name);
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
                "UPDATE persona_magic SET " + field.field + "=" + (field.type == JDBCType.VARCHAR ? "'" + toSet + "'" : toSet) + " WHERE persona_id_fk=" + persona.getPersonaId() + " AND magic_fk='" + magic_name + "';"
        };
    }

    @Override
    public String toString() {
        return "MagicUpdateRow{" +
                "persona=" + MessageUtil.identifyPersona(persona) +
                ", magic_name='" + magic_name + '\'' +
                ", field=" + field +
                ", toSet=" + toSet +
                '}';
    }
}
