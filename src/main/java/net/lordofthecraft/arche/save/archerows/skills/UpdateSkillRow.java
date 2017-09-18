package net.lordofthecraft.arche.save.archerows.skills;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.persona.SkillAttachment;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;

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
        return !row.isUnique() && row instanceof UpdateSkillRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        return null;
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
        PreparedStatement statement = connection.prepareStatement("UPDATE persona_skills SET ?=? WHERE persona_id_fk=? AND skill_id_fk=?");
        statement.setString(1, field.field);
        if (ArcheCore.getPlugin().isUsingSQLite()) {
            switch (field) {
                case XP:
                    statement.setDouble(2, (double) data);
                case VISIBLE:
                    statement.setBoolean(2, (boolean) data);
            }
        } else {
            statement.setObject(2, data, field.type);
        }
        statement.setInt(3, persona.getPersonaId());
        statement.setString(4, skill.getName());
        statement.executeUpdate();
    }

    @Override
    public String[] getInserts() {
        return new String[0];
    }
}
