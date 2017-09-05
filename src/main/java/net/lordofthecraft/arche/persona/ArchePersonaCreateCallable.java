package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.enums.Race;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
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
    private final String world;

    public ArchePersonaCreateCallable(UUID player, int slot, String gender, Race race, String name, Timestamp creationtime, int x, int y, int z, String world) {
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
        PreparedStatement insertPrimary = handler.getConnection().prepareStatement("INSERT INTO persona(player_fk,slot,race_key,gender,name) VALUES (?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

        insertPrimary.setString(1, player.toString());
        insertPrimary.setInt(2, slot);
        insertPrimary.setString(3, race.name());
        insertPrimary.setString(4, gender);
        insertPrimary.setString(5, name);
        insertPrimary.executeUpdate();
        ResultSet rs = insertPrimary.getGeneratedKeys();
        if (rs.next()) {
            int persona_id = rs.getInt(1);
            PreparedStatement insertStats = handler.getConnection().prepareStatement("INSERT INTO persona_stats(persona_id_fk,date_created,last_played)" +
                    " VALUES (?,?,?)");
            PreparedStatement insertVitals = handler.getConnection().prepareStatement("INSERT INTO persona_vitals(persona_id_fk,world,x,y,z,inv,ender_inv)" +
                    " VALUES (?,?,?,?,?,?,?)");
            insertStats.setInt(1, persona_id);
            insertStats.setTimestamp(2, creationtime);
            insertStats.setTimestamp(3, creationtime);
            insertStats.executeUpdate();

            insertVitals.setInt(1, persona_id);
            insertVitals.setString(2, world);
            insertVitals.setInt(3, x);
            insertVitals.setInt(4, y);
            insertVitals.setInt(5, z);
            insertVitals.setString(6, null);
            insertVitals.setString(7, null);
            insertVitals.executeUpdate();

            insertVitals.close();
            insertStats.close();
            insertPrimary.close();
            rs.close();

            return new ArchePersona(persona_id, player, slot, name, race, gender, creationtime);
        }
        return null;
    }
}
