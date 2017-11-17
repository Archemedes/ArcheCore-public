package net.lordofthecraft.arche.save.rows.player;

import org.bukkit.entity.Player;

import net.lordofthecraft.arche.save.rows.SingleStatementRow;

public class ReplacePlayerRow extends SingleStatementRow {
    private final Player player;

    public ReplacePlayerRow(Player player) {
        this.player = player;
    }
    
	@Override
	protected String getStatement() {
		return "REPLACE INTO players(player,player_name) VALUES (?,?)";
	}

	@Override
	protected Object getValueFor(int index) {
		switch(index) {
		case 1: return player.getUniqueId();
		case 2: return player.getName();
		default: throw new IllegalArgumentException();
		}
	}

}
