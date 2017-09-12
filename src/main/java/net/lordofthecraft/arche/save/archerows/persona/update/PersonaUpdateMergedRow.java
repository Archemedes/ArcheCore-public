package net.lordofthecraft.arche.save.archerows.persona.update;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.PersonaTable;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.logging.Level;

public class PersonaUpdateMergedRow implements ArcheMergeableRow {

    final List<PersonaUpdateRow> updateRows = Lists.newArrayList();
    final PersonaTable updateTable;
    Connection connection;

    public PersonaUpdateMergedRow(PersonaUpdateRow row1, PersonaUpdateRow row2) {
        updateTable = row1.updatefield.table;
        updateRows.add(row1);
        updateRows.add(row2);
    }


    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && (row instanceof PersonaUpdateRow
                && ((((PersonaUpdateRow) row).updatefield.table != PersonaTable.MASTER
                && updateTable != PersonaTable.MASTER)
                || ((PersonaUpdateRow) row).updatefield.table == PersonaTable.MASTER
                && updateTable == PersonaTable.MASTER));
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        updateRows.add((PersonaUpdateRow) second);
        return this;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("UPDATE " + updateTable.getTable() + " SET ?=? WHERE persona_id" + (updateTable == PersonaTable.MASTER ? "=?" : "_fk=?"));
            for (PersonaUpdateRow row : updateRows) {
                statement.setString(1, row.updatefield.field());
                if (ArcheCore.usingSQLite()) {
                    switch (row.updatefield) {
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
                            statement.setString(1, (String) row.data);
                            break;
                        case CURRENT:
                            statement.setBoolean(1, (boolean) row.data);
                            break;
                        case STAT_PLAYED:
                        case STAT_CHARS:
                        case STAT_PLAYTIME_PAST:
                        case ICON:
                        case X:
                        case Y:
                        case Z:
                        case FOOD:
                            statement.setInt(1, (int) row.data);
                            break;
                        case STAT_RENAMED:
                        case STAT_CREATION:
                            statement.setTimestamp(1, (Timestamp) row.data);
                            break;
                        case MONEY:
                        case FATIGUE:
                        case MAX_FATIGUE:
                        case HEALTH:
                            statement.setDouble(1, (double) row.data);
                            break;
                        case SATURATION:
                            statement.setFloat(1, (float) row.data);
                            break;
                    }
                } else {
                    statement.setObject(2, row.data, row.updatefield.type);
                }
                statement.setInt(3, row.toupdate.getPersonaId());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            if (statement != null) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] Problematic Persona Update: " + statement.toString());
            }
            throw ex;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] Failed to close update statement in PersonaUpdateMergedRow", e);
            }
        }
    }

    @Override
    public String[] getInserts() {
        return new String[0];
    }
}
