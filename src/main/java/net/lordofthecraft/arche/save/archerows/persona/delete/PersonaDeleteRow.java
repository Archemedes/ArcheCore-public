package net.lordofthecraft.arche.save.archerows.persona.delete;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArcheMergeableRow;
import net.lordofthecraft.arche.save.archerows.ArchePersonaRow;
import net.lordofthecraft.arche.util.MessageUtil;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PersonaDeleteRow implements ArcheMergeableRow, ArchePersonaRow {

    static final String[] deletes = new String[]{
            "DELETE FROM persona_skills WHERE persona_id_fk=",
            "DELETE FROM persona_magics WHERE persona_id_fk=",
            "DELETE FROM persona_vitals WHERE persona_id_fk=",
            "DELETE FROM persona_stats WHERE persona_id_fk=",
            "DELETE FROM persona_attributes WHERE persona_id_fk=",
            "DELETE FROM persona WHERE persona_id="
    };

    final Persona persona;
    private Connection conn = null;

    public PersonaDeleteRow(Persona persona) {
        this.persona = persona;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public boolean canMerge(ArcheMergeableRow row) {
        return !row.isUnique() && row instanceof PersonaDeleteRow;
    }

    @Override
    public ArcheMergeableRow merge(ArcheMergeableRow second) {
        if (second.isUnique()) {
            throw new IllegalArgumentException("Can't merge a unique row");
        }
        return new MultiPersonaDeleteRow(this, (PersonaDeleteRow) second);
    }

    @Override
    public void setConnection(Connection connection) {
        this.conn = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        if (ArcheCore.getPlugin().isUsingSQLite()) {
            Statement statement = conn.createStatement();
            for (String delete : deletes) {
                statement.executeUpdate(delete + persona.getPersonaId());
            }
            statement.close();
        } else {
            CallableStatement statement = conn.prepareCall("{call persona_delete(?)}");
            statement.setInt(1, persona.getPersonaId());
            statement.executeUpdate();
            statement.close();
        }
    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{persona};
    }

    @Override
    public String[] getInserts() {
        if (ArcheCore.getPlugin().isUsingSQLite()) {
            String[] strings = new String[6];
            for (int i = 0; i < deletes.length; ++i) {
                strings[i] = deletes[i] + persona.getPersonaId();
            }
            return strings;
        }
        return new String[]{"{call persona_delete(" + persona.getPersonaId() + ")}"};
    }

    @Override
    public String toString() {
        return "PersonaDeleteRow{" +
                "persona=" + MessageUtil.identifyPersona(persona) +
                '}';
    }
}
