package net.lordofthecraft.arche.save.rows.persona.insert;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.ArchePersonaRow;
import net.lordofthecraft.arche.save.rows.ArchePreparedStatementRow;
import net.lordofthecraft.arche.util.MessageUtil;
import net.lordofthecraft.arche.util.SQLUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PersonaInsertRow implements ArchePreparedStatementRow, ArchePersonaRow {

    private final Persona persona;
    private Connection connection = null;

    public PersonaInsertRow(Persona persona) {
        this.persona = persona;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement stat = connection.prepareStatement("INSERT INTO persona(persona_id,player_fk,slot,race,name,gender,last_played) " +
                "VALUES (?,?,?,?,?,?,?)");

        stat.setInt(1, persona.getPersonaId());
        stat.setString(2, persona.getPlayerUUID().toString());
        stat.setInt(3, persona.getSlot());
        stat.setString(4, persona.getRace().name());
        stat.setString(5, persona.getName());
        stat.setString(6, persona.getGender());
        stat.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
        stat.executeUpdate();
        stat.close();
        
        stat = connection.prepareStatement("INSERT INTO persona_stats(persona_id_fk,renamed,date_created) VALUES (?,?,?)");
        stat.setInt(1, persona.getPersonaId());
        stat.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
        stat.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        stat.executeUpdate();
        stat.close();
    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{persona};
    }

    @Override
    public String[] getInserts() {
        if (persona.getPlayer() == null) {
            return new String[0];
        }
        return new String[]{
                "INSERT INTO persona(persona_id,player_fk,slot,race,name,gender) " +
                        "VALUES (" + persona.getPersonaId() + ",'"
                        + persona.getPlayerUUID().toString()
                        + "'," + persona.getSlot()
                        + ",'" + persona.getRace().name()
                        + "','" + SQLUtil.mysqlTextEscape(persona.getName())
                        + "','" + SQLUtil.mysqlTextEscape(persona.getGender())
                        + "');",
                "INSERT INTO persona_stats(persona_id_fk,renamed,date_created,last_played) VALUES (" + persona.getPersonaId()
                        + "," + new Timestamp(System.currentTimeMillis()).toString()
                        + "," + new Timestamp(System.currentTimeMillis()).toString()
                        + "," + new Timestamp(System.currentTimeMillis()).toString()
                        + ");"
        };
    }

    @Override
    public String toString() {
        return "PersonaInsertRow{" +
                "persona=" + MessageUtil.identifyPersona(persona) +
                '}';
    }
}
