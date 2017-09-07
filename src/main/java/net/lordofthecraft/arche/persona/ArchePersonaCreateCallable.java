package net.lordofthecraft.arche.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.enums.Race;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Level;

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
        ArcheCore.getPlugin().getLogger().info("Starting ArchePersonaCreateCallable: " + toString());
        try {
            ArcheCore.getPlugin().getLogger().info("Inside try now, grabbing timer.");
            ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
            if (timer != null) {
                timer.startTiming("Persona Create " + player.toString() + "@" + slot);
            }
            long time = System.currentTimeMillis();
            ArcheCore.getPlugin().getLogger().info("The current time is " + time);

            SQLHandler handler = ArcheCore.getSQLControls();
            ArcheCore.getPlugin().getLogger().info("Got SQLhandler. Getting connection now...");
            Connection conn = handler.getConnection();
            ArcheCore.getPlugin().getLogger().info("Setting autocommit to false... Time: " + (System.currentTimeMillis() - time) + "ms");
            conn.setAutoCommit(false);
            ArcheCore.getPlugin().getLogger().info("Finished turning off autocommit. Preparing statement. Time: " + (System.currentTimeMillis() - time) + "ms");
        /*
                statement.execute("CREATE TABLE IF NOT EXISTS persona (" +
                "persona_id CHAR(36)," +
                "player_fk CHAR(36) NOT NULL," +
                "slot INT UNSIGNED NOT NULL," +
                "race VARCHAR(255) NOT NULL," +
                "name TEXT," +
                "race_header TEXT DEFAULT NULL," +
                "gender TEXT DEFAULT 'Other'," +
                "p_type TEXT DEFAULT 'NORMAL'," +
                "descr TEXT DEFAULT NULL," +
                "prefix TEXT DEFAULT NULL," +
                "curr BOOLEAN DEFAULT FALSE," +
                "money DOUBLE DEFAULT 0.0," +
                "skin INT DEFAULT -1," +
                "profession VARCHAR(255) DEFAULT NULL," +
                "fatigue DOUBLE DEFAULT 0.0," +
                "max_fatigue DOUBLE DEFAULT 100.00," +
                "PRIMARY KEY (persona_id)," +
                "FOREIGN KEY (player_fk) REFERENCES players (player) ON UPDATE CASCADE ON DELETE RESTRICT," +
                "FOREIGN KEY (profession) REFERENCES skills (skill_id) ON UPDATE CASCADE ON DELETE SET NULL," +
                "FOREIGN KEY (skin) REFERENCES persona_skins (skin_id) ON UPDATE CASCADE ON DELETE SET NULL" +
                ")" +
                end);
         */
            PreparedStatement insertPrimary = conn.prepareStatement("INSERT INTO persona(persona_id,player_fk,slot,race,gender,name) VALUES (?,?,?,?,?,?)");
            ArcheCore.getPlugin().getLogger().info("Statement prepared. Setting values. Time: " + (System.currentTimeMillis() - time) + "ms");
            UUID newPersonaId = UUID.randomUUID();
            insertPrimary.setString(1, newPersonaId.toString());
            insertPrimary.setString(2, player.toString());
            insertPrimary.setInt(3, slot);
            insertPrimary.setString(4, race.name());
            insertPrimary.setString(5, gender);
            insertPrimary.setString(6, name);
            ArcheCore.getPlugin().getLogger().info("values set. Executing. Time: " + (System.currentTimeMillis() - time) + "ms");
            insertPrimary.executeUpdate();
            ArcheCore.getPlugin().getLogger().info("Persona entry has been created. Preparing statements. Time: " + (System.currentTimeMillis() - time) + "ms");

            PreparedStatement insertStats = conn.prepareStatement("INSERT INTO persona_stats(persona_id_fk,date_created,last_played)" +
                    " VALUES (?,?,?)");
            ArcheCore.getPlugin().getLogger().info("Insert statement created for stats. Creating one for vitals... Time: " + (System.currentTimeMillis() - time) + "ms");
            PreparedStatement insertVitals = conn.prepareStatement("INSERT INTO persona_vitals(persona_id_fk,world,x,y,z,inv,ender_inv)" +
                    " VALUES (?,?,?,?,?,?,?)");
            ArcheCore.getPlugin().getLogger().info("Vitals insert statement created, setting stats values. Time: " + (System.currentTimeMillis() - time) + "ms");
            insertStats.setString(1, newPersonaId.toString());
            insertStats.setTimestamp(2, creationtime);
            insertStats.setTimestamp(3, creationtime);
            ArcheCore.getPlugin().getLogger().info("Values for stats set. Executing update.... Time: " + (System.currentTimeMillis() - time) + "ms");
            insertStats.executeUpdate();
            ArcheCore.getPlugin().getLogger().info("Stats have been inserted. Now binding values to vitals.... Time: " + (System.currentTimeMillis() - time) + "ms");

            insertVitals.setString(1, newPersonaId.toString());
            insertVitals.setString(2, world.toString());
            insertVitals.setInt(3, x);
            insertVitals.setInt(4, y);
            insertVitals.setInt(5, z);
            insertVitals.setString(6, null);
            insertVitals.setString(7, null);
            ArcheCore.getPlugin().getLogger().info("Finished binding variables. Time: " + (System.currentTimeMillis() - time) + "ms");
            insertVitals.executeUpdate();
            ArcheCore.getPlugin().getLogger().info("Executed vitals insert. Committing... Time: " + (System.currentTimeMillis() - time) + "ms");

            conn.commit();

            ArcheCore.getPlugin().getLogger().info("Finished committing. Time: " + (System.currentTimeMillis() - time) + "ms");

            insertVitals.close();
            insertStats.close();
            insertPrimary.close();

            ArcheCore.getPlugin().getLogger().info("Closed all statements. Time: " + (System.currentTimeMillis() - time) + "ms");

            if (handler instanceof WhySQLHandler) {
                conn.close();
            } else {
                conn.setAutoCommit(true);
            }

            if (timer != null) {
                timer.stopTiming("Persona create " + player.toString() + "@" + slot);
            }

            return new ArchePersona(newPersonaId, player, slot, name, race, gender, creationtime);
        } catch (Exception e) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "We threw an exception while creating a persona!", e);
            throw e;
        }
    }

    @Override
    public String toString() {
        return "ArchePersonaCreateCallable{" +
                "player=" + player +
                ", slot=" + slot +
                ", gender='" + gender + '\'' +
                ", race=" + race +
                ", name='" + name + '\'' +
                ", creationtime=" + creationtime +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                ", world=" + world +
                '}';
    }
}
