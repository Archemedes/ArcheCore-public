package net.lordofthecraft.arche.save.archerows.skills;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Skill;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SkillRow implements ArcheMergeableRow, ArchePersonaRow {

    final Persona persona;
    final Skill skill;
    final double xp;
    final boolean visible;
    private Connection connection = null;

    public SkillRow(Persona persona, Skill skill, double xp, boolean visible) {
        this.persona = persona;
        this.skill = skill;
        this.xp = xp;
        this.visible = visible;
    }

    @Override
    public boolean isUnique() {
        return false;
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
        return new MultiSkillRow(this, (SkillRow) second);
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO persona_skills (persona_id_fk,skill_id_fk,xp,visible) VALUES (?,?,?,?)");
        statement.setInt(1, persona.getPersonaId());
        statement.setString(2, skill.getName());
        statement.setDouble(3, xp);
        statement.setBoolean(4, visible);
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public String[] getInserts() {
        return new String[]{"INSERT INTO persona_skills (persona_id_fk,skill_id_fk,xp,visible) VALUES (" + persona.getPersonaId() + ",'" + SQLUtil.mysqlTextEscape(skill.getName()) + "'," + xp + "," + visible + ");"};
    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{persona};
    }

    @Override
    public String toString() {
        return "SkillRow{" +
                "persona=" + MessageUtil.identifyPersona(persona) +
                ", skill=" + skill.getName() +
                ", xp=" + xp +
                ", visible=" + visible +
                '}';
    }
}
