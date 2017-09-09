package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.save.tasks.ArcheTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class PersonaInsertTask extends ArcheTask {

    private final int persona_id;
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

    public PersonaInsertTask(int persona_id, UUID player, int slot, String gender, Race race, String name, Timestamp creationtime, int x, int y, int z, UUID world) {
        this.persona_id = persona_id;
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
    public void run() {
        Connection conn = handle.getConnection();
        try {
            if (handle instanceof WhySQLHandler) {
                conn.setAutoCommit(false);
            }
            PreparedStatement stat = conn.prepareStatement("INSERT INTO persona(persona_id,player_fk,slot,race,name,gender,skin) " +
                    "VALUES (LAST_INSERT_ID(?),?,?,?,?,?,'NULL')");

            stat.setInt(1, persona_id);
            stat.setString(2, player.toString());
            stat.setInt(3, slot);
            stat.setString(4, race.name());
            stat.setString(5, name);
            stat.setString(6, gender);
            stat.executeUpdate();
            stat.close();

            stat = conn.prepareStatement("INSERT INTO persona_vitals(persona_id_fk,world,x,y,z,inv,ender_inv) VALUES (?,?,?,?,?,?,?)");
            stat.setInt(1, persona_id);
            stat.setString(2, world.toString());
            stat.setInt(3, x);
            stat.setInt(4, y);
            stat.setInt(5, z);
            stat.setString(6, null);
            stat.setString(7, null);
            stat.executeUpdate();
            stat.close();

            stat = conn.prepareStatement("INSERT INTO persona_stats(persona_id_fk,renamed,date_created,last_played) VALUES (?,?,?,?)");
            stat.setInt(1, persona_id);
            stat.setTimestamp(2, new Timestamp(0));
            stat.setTimestamp(3, creationtime);
            stat.setTimestamp(4, creationtime);
            stat.executeUpdate();
            stat.close();

            if (handle instanceof WhySQLHandler) {
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (handle instanceof WhySQLHandler) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
