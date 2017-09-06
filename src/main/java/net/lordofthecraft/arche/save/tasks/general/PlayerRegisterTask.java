package net.lordofthecraft.arche.save.tasks.general;

import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.save.tasks.StatementTask;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created on 9/3/2017
 *
 * @author 501warhead
 */
public class PlayerRegisterTask extends StatementTask {

    private final UUID player;

    public PlayerRegisterTask(UUID player) {
        this.player = player;
    }

    @Override
    protected void setValues() throws SQLException {
        stat.setString(1, player.toString());
    }

    @Override
    protected String getQuery() {
        if (handle instanceof ArcheSQLiteHandler) {
            return "INSERT OR IGNORE INTO players (player) VALUES (?)";
        }
        return "INSERT IGNORE INTO players(player) VALUES (?)";
    }
}
