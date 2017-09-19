package net.lordofthecraft.arche.save.archerows.skills;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.SkillAttachment;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.save.archerows.ArcheRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class MultiUpdateSkillRow implements ArcheMergeableRow, ArchePersonaRow {

    private final List<UpdateSkillRow> rows = Lists.newArrayList();
    private final List<Persona> personas = Lists.newArrayList();
    private final SkillAttachment.Field field;
    private Connection connection = null;

    public MultiUpdateSkillRow(UpdateSkillRow row1, UpdateSkillRow row2) {
        field = row1.field;
        rows.add(row1);
        rows.add(row2);
        personas.addAll(Arrays.asList(row1.getPersonas()));
        personas.addAll(Arrays.asList(row2.getPersonas()));
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && (row instanceof UpdateSkillRow && ((UpdateSkillRow) row).field == field);
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        rows.add((UpdateSkillRow) second);
        personas.addAll(Arrays.asList(((UpdateSkillRow) second).getPersonas()));
        return this;
    }

    @Override
    public Persona[] getPersonas() {
        return (Persona[]) personas.toArray();
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("UPDATE persona_skills SET " + field.field + "=? WHERE persona_id_fk=? AND skill_id_fk=?");
            for (UpdateSkillRow row : rows) {
                if (ArcheCore.getPlugin().isUsingSQLite()) {
                    switch (row.field) {
                        case XP:
                            statement.setDouble(1, (double) row.data);
                        case VISIBLE:
                            statement.setBoolean(1, (boolean) row.data);
                    }
                } else {
                    statement.setObject(1, row.data, row.field.type);
                }
                statement.setInt(2, row.persona.getPersonaId());
                statement.setString(3, row.skill.getName());
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (SQLException ex) {
            if (statement != null) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[Consumer] Problematic Statement: " + statement.toString());
            }
            throw ex;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[Consumer] Failed to close out PreparedStatement for " + getClass().getSimpleName() + "!", ex);
            }
        }
    }

    @Override
    public String[] getInserts() {
        ArrayList<String> s = Lists.newArrayList();
        for (ArcheRow row : rows) {
            s.addAll(Arrays.asList(row.getInserts()));
        }
        return (String[]) s.toArray();
    }
}
