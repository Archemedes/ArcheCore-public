package net.lordofthecraft.arche.save.rows.player;

import net.lordofthecraft.arche.save.rows.SingleStatementRow;
import org.bukkit.entity.Player;

public class ReplacePlayerRow extends SingleStatementRow {
    private final Player player;

    public ReplacePlayerRow(Player player) {
        this.player = player;
    }

    @Override
    protected String getStatement() {
        return "INSERT INTO players (player,player_name) VALUES (?,?) ON DUPLICATE KEY UPDATE player_name=?";
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1:
                return player.getUniqueId();
            case 2:
            case 3:
                return player.getName();
            default:
                throw new IllegalArgumentException(index + " was passed in.");
        }
    }

}
