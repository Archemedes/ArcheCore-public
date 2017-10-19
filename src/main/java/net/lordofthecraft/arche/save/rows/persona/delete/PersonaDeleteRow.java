package net.lordofthecraft.arche.save.rows.persona.delete;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.ArchePersonaRow;
import net.lordofthecraft.arche.save.rows.ArchePreparedStatementRow;
import net.lordofthecraft.arche.util.MessageUtil;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class PersonaDeleteRow implements ArchePreparedStatementRow, ArchePersonaRow {

    static final String[] deletes = new String[]{
            "DELETE FROM persona_skills WHERE persona_id_fk=",
            "DELETE FROM persona_magics WHERE persona_id_fk=",
            "DELETE FROM persona_vitals WHERE persona_id_fk=",
            "DELETE FROM persona_stats WHERE persona_id_fk=",
            "DELETE FROM persona_tags WHERE persona_id_fk=",
            "DELETE FROM persona_name WHERE persona_id_fk=",
            "DELETE FROM persona_attributes WHERE persona_id_fk=",
            "DELETE FROM per_persona_skins WHERE persona_id_fk=",
            "DELETE FROM persona WHERE persona_id="
    };

    final Persona persona;
    private Connection conn = null;

    public PersonaDeleteRow(Persona persona) {
        this.persona = persona;
    }

    @Override
    public void setConnection(Connection connection) {
        this.conn = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        if (ArcheCore.getPlugin().logsPersonaDeletions()) {
            CallableStatement statement = conn.prepareCall("{call persona_delete(?)}");
            statement.setInt(1, persona.getPersonaId());
            statement.executeUpdate();
            statement.close();
        } else {
            Statement statement = conn.createStatement();
            for (String delete : deletes) {
                statement.executeUpdate(delete + persona.getPersonaId());
            }
            statement.close();
        }
    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{persona};
    }

    @Override
    public String[] getInserts() {
        if (!ArcheCore.getPlugin().logsPersonaDeletions()) {
            String[] strings = new String[deletes.length];
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
