package net.lordofthecraft.arche.save.tasks.persona;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.save.tasks.ArcheTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Created on 9/3/2017
 *
 * @author 501warhead
 */
public class UpdateFatigueTask extends ArcheTask {

    private final double fatigue;
    private final UUID personaid;
    private final String username;

    public UpdateFatigueTask(double fatigue, UUID personaid, String username) {
        this.fatigue = fatigue;
        this.personaid = personaid;
        this.username = username;
    }

    @Override
    public void run() {
        Connection newConn = handle.getConnection();
        try {
            PreparedStatement stat = newConn.prepareStatement("UPDATE persona SET fatigue=? WHERE persona_id=?");
            stat.setDouble(1, fatigue);
            stat.setString(2, personaid.toString());
            stat.executeUpdate();
            stat.close();
            if (handle instanceof WhySQLHandler) {
                newConn.close();
            }
        } catch (SQLException e) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "Failed to update the fatigue for persona id " + personaid + ", a persona of " + username);
        }

    }
}
