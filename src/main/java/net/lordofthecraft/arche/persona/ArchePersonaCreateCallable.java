package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.enums.Race;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Creates a persona on a separate thread, returning the result.
 *
 * @author 501warhead
 */
public class ArchePersonaCreateCallable implements Callable<ArchePersona> {

    private final UUID player;
    private final int slot;
    private final String gender;
    private final Race race;
    private final String name;
    private final Timestamp creationtime;
    private final int x;
    private final int y;
    private final int z;
    private final UUID world;

    public ArchePersonaCreateCallable(UUID player, int slot, String gender, Race race, String name, Timestamp creationtime, int x, int y, int z, UUID world) {
        this.player = player;
        this.slot = slot;
        this.gender = gender;
        this.race = race;
        this.name = name;
        this.creationtime = creationtime;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

    @Override
    public ArchePersona call() throws Exception {
        SQLHandler handler = ArcheCore.getSQLControls();
        Connection conn = handler.getConnection();
        conn.setAutoCommit(false);
        PreparedStatement insertPrimary = conn.prepareStatement("INSERT INTO persona(persona_id,player_fk,slot,race,gender,name) VALUES (?,?,?,?,?,?)");

        UUID newPersonaId = UUID.randomUUID();
        insertPrimary.setString(1, newPersonaId.toString());
        insertPrimary.setString(2, player.toString());
        insertPrimary.setInt(3, slot);
        insertPrimary.setString(4, race.name());
        insertPrimary.setString(5, gender);
        insertPrimary.setString(6, name);
        insertPrimary.executeUpdate();

        PreparedStatement insertStats = conn.prepareStatement("INSERT INTO persona_stats(persona_id_fk,date_created,last_played)" +
                " VALUES (?,?,?)");
        PreparedStatement insertVitals = conn.prepareStatement("INSERT INTO persona_vitals(persona_id_fk,world,x,y,z,inv,ender_inv)" +
                " VALUES (?,?,?,?,?,?,?)");
        insertStats.setString(1, newPersonaId.toString());
        insertStats.setTimestamp(2, creationtime);
        insertStats.setTimestamp(3, creationtime);
        insertStats.executeUpdate();

        insertVitals.setString(1, newPersonaId.toString());
        insertVitals.setString(2, world.toString());
        insertVitals.setInt(3, x);
        insertVitals.setInt(4, y);
        insertVitals.setInt(5, z);
        insertVitals.setString(6, null);
        insertVitals.setString(7, null);
        insertVitals.executeUpdate();

        conn.commit();

        insertVitals.close();
        insertStats.close();
        insertPrimary.close();

        if (handler instanceof WhySQLHandler) {
            conn.close();
        } else {
            conn.setAutoCommit(true);
        }

        return new ArchePersona(newPersonaId, player, slot, name, race, gender, creationtime);
    }
}
