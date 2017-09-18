package net.lordofthecraft.arche.save.archerows.player;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.archerows.ArchePreparedStatementRow;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerInsertRow implements ArchePreparedStatementRow {

    private final Player player;
    private Connection connection = null;

    public PlayerInsertRow(Player player) {
        this.player = player;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT " + (ArcheCore.getPlugin().isUsingSQLite() ? "OR IGNORE " : "IGNORE ") + " INTO players(player) VALUES (?)");
        statement.setString(1, player.getUniqueId().toString());
        statement.executeUpdate();
    }

    @Override
    public String[] getInserts() {
        return new String[]{"INSERT " + (ArcheCore.getPlugin().isUsingSQLite() ? "OR IGNORE " : "IGNORE ") + " INTO players(player) VALUES ('" + player.getUniqueId().toString() + "');"};
    }

    @Override
    public String toString() {
        return "PlayerInsertRow{" +
                "player=" + player.getUniqueId() +
                ", playerName='" + player.getName() + '\'' +
                '}';
    }
}
