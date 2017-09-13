package net.lordofthecraft.arche.save.archerows.persona.update;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.persona.ArchePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PersonaUpdateRow implements ArcheMergeableRow, ArchePersonaRow {

    final Persona toupdate;
    final PersonaField updatefield;
    final Object data;
    final boolean solorun;
    Connection conn = null;


    public PersonaUpdateRow(Persona toupdate, PersonaField updatefield, Object data, boolean solorun) {
        this.toupdate = toupdate;
        this.updatefield = updatefield;
        this.data = data;
        this.solorun = solorun;
    }

    @Override
    public boolean isUnique() {
        return solorun;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !isUnique()
                && (row instanceof PersonaUpdateRow
                && ((PersonaUpdateRow) row).updatefield.table == updatefield.table);
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Can't merge a unique row");
        }
        return new MultiPersonaUpdateRow(this, (PersonaUpdateRow) second);
    }

    @Override
    public void setConnection(Connection connection) {
        this.conn = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = updatefield.getStatement(conn);
        statement.setString(1, updatefield.field());
        if (ArcheCore.usingSQLite()) {
            switch (updatefield) {
                case PREFIX:
                case NAME:
                case RACE:
                case RACE_REAL:
                case DESCRIPTION:
                case TYPE:
                case GENDER:
                case WORLD:
                case INV:
                case ENDERINV:
                case SKILL_SELECTED:
                case POTIONS:
                    statement.setString(1, (String) data);
                    break;
                case CURRENT:
                    statement.setBoolean(1, (boolean) data);
                    break;
                case STAT_PLAYED:
                case STAT_CHARS:
                case STAT_PLAYTIME_PAST:
                case ICON:
                case X:
                case Y:
                case Z:
                case FOOD:
                    statement.setInt(1, (int) data);
                    break;
                case STAT_RENAMED:
                case STAT_CREATION:
                    statement.setTimestamp(1, (Timestamp) data);
                    break;
                case MONEY:
                case FATIGUE:
                case MAX_FATIGUE:
                case HEALTH:
                    statement.setDouble(1, (double) data);
                    break;
                case SATURATION:
                    statement.setFloat(1, (float) data);
                    break;
            }
        } else {
            statement.setObject(2, data, updatefield.type);
        }
        statement.setInt(3, toupdate.getPersonaId());
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{toupdate};
    }

    @Override
    public String[] getInserts() {
        return new String[0];
    }

    @Override
    public String toString() {
        return "PersonaUpdateRow{" +
                "toupdate=" + MessageUtil.identifyPersona(toupdate) +
                ", updatefield=" + updatefield +
                ", data=" + data +
                ", solorun=" + solorun +
                '}';
    }
}
