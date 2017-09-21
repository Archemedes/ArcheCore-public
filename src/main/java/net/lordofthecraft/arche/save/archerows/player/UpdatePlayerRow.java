package net.lordofthecraft.arche.save.archerows.player;

import net.lordofthecraft.arche.save.archerows.ArchePreparedStatementRow;
import net.lordofthecraft.arche.util.SQLUtil;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UpdatePlayerRow implements ArchePreparedStatementRow {

    private final Player player;
    private Connection connection = null;

    public UpdatePlayerRow(Player player) {
        this.player = player;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void executeStatements() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("UPDATE players SET player_name=? WHERE player=?");
        statement.setString(1, player.getName());
        statement.setString(2, player.getUniqueId().toString());
        statement.executeUpdate();
        statement.close();
    }

    @Override
    public String[] getInserts() {
        return new String[]{"UPDATE players SET player_name='" + SQLUtil.mysqlTextEscape(player.getName()) + "' WHERE player='" + player.getUniqueId().toString() + "';"};
    }
}
