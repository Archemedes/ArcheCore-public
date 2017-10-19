package net.lordofthecraft.arche.save.rows.persona.update;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.PersonaTable;
import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.rows.ArchePersonaRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MultiPersonaUpdateRow implements ArcheMergeableRow, ArchePersonaRow {

    final List<PersonaUpdateRow> updateRows = Lists.newArrayList();
    final List<OfflinePersona> personas = Lists.newArrayList();
    final PersonaField field;
    Connection connection;

    public MultiPersonaUpdateRow(PersonaUpdateRow row1, PersonaUpdateRow row2) {
        field = row1.updatefield;
        updateRows.add(row1);
        updateRows.add(row2);
        personas.addAll(Lists.newArrayList(row1.getPersonas()));
        personas.addAll(Lists.newArrayList(row2.getPersonas()));
    }


    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && (row instanceof PersonaUpdateRow
                && ((PersonaUpdateRow) row).updatefield == field);
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
            statement = connection.prepareStatement("UPDATE " + field.table.getTable() + " SET " + field.field() + "=? WHERE persona_id" + (field.table == PersonaTable.MASTER ? "=?" : "_fk=?"));
            for (PersonaUpdateRow row : updateRows) {
                if (!row.toupdate.isDeleted()) {
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
                        statement.setObject(1, row.data, row.updatefield.type);
                    }
                    statement.setInt(2, row.toupdate.getPersonaId());
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            if (statement != null) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[Consumer] Problematic Persona Update: " + statement.toString());
            }
            throw ex;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[Consumer] Failed to close update statement in MultiPersonaUpdateRow", e);
            }
        }
    }

    @Override
    public String[] getInserts() {
        List<String> list = Lists.newArrayList();
        for (PersonaUpdateRow row : updateRows) {
            list.addAll(Arrays.asList(row.getInserts()));
        }
        return (String[]) list.toArray();
    }

    @Override
    public Persona[] getPersonas() {
        return (Persona[]) personas.toArray();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("MultiPersonaUpdateRow[");
        for (PersonaUpdateRow row : updateRows) {
            builder.append(" ").append(row.toString()).append(" ");
        }
        builder.append("]");
        return builder.toString();
    }
}
