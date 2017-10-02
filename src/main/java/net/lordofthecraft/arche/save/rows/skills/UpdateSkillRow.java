package net.lordofthecraft.arche.save.rows.skills;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.persona.SkillAttachment;
import net.lordofthecraft.arche.save.rows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.rows.ArchePersonaRow;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdateSkillRow implements ArcheMergeableRow, ArchePersonaRow {

    final Persona persona;
    final Skill skill;
    final SkillAttachment.Field field;
    final Object data;
    private Connection connection = null;

    public UpdateSkillRow(Persona persona, Skill skill, SkillAttachment.Field field, Object data) {
        this.persona = persona;
        this.skill = skill;
        this.field = field;
        this.data = data;
    }

    @Override
    public boolean isUnique() {
        return false;
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
        return new MultiUpdateSkillRow(this, (UpdateSkillRow) second);
    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{persona};
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE persona_skills SET " + field.field + "=? WHERE persona_id_fk=? AND skill_id_fk=?");

        if (ArcheCore.getPlugin().isUsingSQLite()) {
            switch (field) {
                case XP:
                    statement.setDouble(1, (double) data);
                case VISIBLE:
                    statement.setBoolean(1, (boolean) data);
            }
        } else {
            statement.setObject(1, data, field.type);
        }
        statement.setInt(2, persona.getPersonaId());
        statement.setString(3, skill.getName());
        statement.executeUpdate();
    }

    @Override
    public String[] getInserts() {
        return new String[]{"UPDATE persona_skills SET " + field.field + "=" + (data instanceof String ? "'" + SQLUtil.mysqlTextEscape((String) data) + '\'' : data) + " WHERE persona_id_fk=" + persona.getPersonaId() + " AND skill_id_fk='" + SQLUtil.mysqlTextEscape(skill.getName()) + "';"};
    }
}
