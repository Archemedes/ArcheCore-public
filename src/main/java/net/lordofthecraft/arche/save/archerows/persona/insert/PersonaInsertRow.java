package net.lordofthecraft.arche.save.archerows.persona.insert;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.archerows.ArchePreparedStatementRow;
import net.lordofthecraft.arche.save.archerows.persona.ArchePersonaRow;
import org.bukkit.block.Block;

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
        PreparedStatement stat = connection.prepareStatement("INSERT INTO persona(persona_id,player_fk,slot,race,name,gender,skin) " +
                "VALUES (LAST_INSERT_ID(?),?,?,?,?,?,'NULL')");

        stat.setInt(1, persona.getPersonaId());
        stat.setString(2, persona.getPlayerUUID().toString());
        stat.setInt(3, persona.getId());
        stat.setString(4, persona.getRace().name());
        stat.setString(5, persona.getName());
        stat.setString(6, persona.getGender());
        stat.executeUpdate();
        stat.close();

        stat = connection.prepareStatement("INSERT INTO persona_vitals(persona_id_fk,world,x,y,z,inv,ender_inv) VALUES (?,?,?,?,?,?,?)");
        stat.setInt(1, persona.getPersonaId());
        Block b = persona.getPlayer().getLocation().getBlock();
        stat.setString(2, b.getWorld().getUID().toString());
        stat.setInt(3, b.getX());
        stat.setInt(4, b.getY());
        stat.setInt(5, b.getZ());
        stat.setString(6, null);
        stat.setString(7, null);
        stat.executeUpdate();
        stat.close();

        stat = connection.prepareStatement("INSERT INTO persona_stats(persona_id_fk,renamed,date_created,last_played) VALUES (?,?,?,?)");
        stat.setInt(1, persona.getPersonaId());
        stat.setTimestamp(2, new Timestamp(0));
        stat.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
        stat.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
        stat.executeUpdate();
        stat.close();
    }

    @Override
    public Persona[] getPersonas() {
        return new Persona[]{persona};
    }

    @Override
    public String[] getInserts() {
        return new String[0];
    }
}
