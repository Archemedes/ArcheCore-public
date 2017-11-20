package net.lordofthecraft.arche.save.rows.skin;

import net.lordofthecraft.arche.save.rows.SingleStatementRow;
import net.lordofthecraft.arche.skin.ArcheSkin;

import java.util.UUID;

public class DeleteSkinRow extends SingleStatementRow {
    private final UUID player;
    private final int slot;

    public DeleteSkinRow(ArcheSkin skin) {
        this.player = skin.getOwner();
        this.slot = skin.getIndex();
    }

    @Override
    protected String getStatement() {
        return "DELETE FROM persona_skins WHERE player=? AND slot=?";
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1:
                return player;
            case 2:
                return slot;
            default:
                throw new IllegalArgumentException();
        }
    }

}
