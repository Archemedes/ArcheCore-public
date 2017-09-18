package net.lordofthecraft.arche.save.archerows.skills;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DelSkillRow implements ArcheMergeableRow, ArchePersonaRow {

    final Persona persona;
    final Skill skill;
    private Connection connection = null;

    public DelSkillRow(Persona persona, Skill skill) {
        this.persona = persona;
        this.skill = skill;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && row instanceof DelSkillRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Cannot merge unique rows");
        }
        return new MultiDelSkillRow(this, (DelSkillRow) second);
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
        PreparedStatement statement = connection.prepareStatement("DELETE FROM persona_skills WHERE persona_id_fk=? AND skill_id_fk=?");
        statement.setInt(1, persona.getPersonaId());
        statement.setString(2, skill.getName());
    }

    @Override
    public String[] getInserts() {
        return new String[]{"DELETE FROM persona_skills WHERE persona_id_fk=" + persona.getPersonaId() + " AND skill_id_fk='" + skill.getName() + "';"};
    }

    @Override
    public String toString() {
        return "DelSkillRow{" +
                "persona=" + MessageUtil.identifyPersona(persona) +
                ", skill=" + skill.getName() +
                '}';
    }
}
