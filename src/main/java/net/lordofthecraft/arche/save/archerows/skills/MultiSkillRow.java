package net.lordofthecraft.arche.save.archerows.skills;

import com.google.common.collect.Lists;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
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

public class MultiSkillRow implements ArcheMergeableRow, ArchePersonaRow {

    private final List<SkillRow> rows = Lists.newArrayList();
    private final List<Persona> personas = Lists.newArrayList();
    private Connection connection = null;

    public MultiSkillRow(SkillRow row1, SkillRow row2) {
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
        return !row.isUnique() && row instanceof SkillRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        rows.add((SkillRow) second);
        personas.addAll(Arrays.asList(((SkillRow) second).getPersonas()));
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
            statement = connection.prepareStatement("INSERT INTO persona_skills (persona_id_fk,skill_id_fk,xp,visible) VALUES (?,?,?,?)");
            for (SkillRow row : rows) {
                statement.setInt(1, row.persona.getPersonaId());
                statement.setString(2, row.skill.getName());
                statement.setDouble(3, row.xp);
                statement.setBoolean(4, row.visible);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException ex) {
            if (statement != null) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] Problematic Statement: " + statement.toString());
            }
            throw ex;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ex) {
                ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "[ArcheCore Consumer] Failed to close out PreparedStatement for " + getClass().getSimpleName() + "!", ex);
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
