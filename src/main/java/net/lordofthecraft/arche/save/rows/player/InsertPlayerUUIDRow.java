package net.lordofthecraft.arche.save.rows.player;

import net.lordofthecraft.arche.save.rows.SingleStatementRow;

import java.util.UUID;

public class InsertPlayerUUIDRow extends SingleStatementRow {

    private final UUID id;
    private final String playername;

    public InsertPlayerUUIDRow(UUID id, String playername) {
        this.id = id;
        this.playername = playername;
    }

    @Override
    protected String getStatement() {
        return "INSERT IGNORE INTO players (player,player_name) VALUES (?,?)";
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1:
                return id.toString();
            case 2:
                return playername;
            default:
                throw new IllegalArgumentException(index + " is out of bounds.");
        }
    }

    @Override
    public String toString() {
        return "InsertPlayerUUIDRow{" +
                "id=" + id +
                ", playername=" + playername +
                '}';
    }
}
