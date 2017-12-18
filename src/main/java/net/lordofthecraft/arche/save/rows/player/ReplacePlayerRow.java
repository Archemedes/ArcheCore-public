package net.lordofthecraft.arche.save.rows.player;

import java.util.UUID;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class ReplacePlayerRow extends SingleStatementRow {
    private final UUID uuid;
    private final String name;

    public ReplacePlayerRow(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    protected String getStatement() {
        if (ArcheCore.usingSQLite()) {
            return "REPLACE INTO players (player,player_name) VALUES (?,?)";
        } else {
            return "INSERT INTO players (player,player_name) VALUES (?,?) ON DUPLICATE KEY UPDATE player_name=?";
        }
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1: return uuid;
            case 2:
            case 3:
                return name;
            default:
                throw new IllegalArgumentException(index + " was passed in.");
        }
    }

}
