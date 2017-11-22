package net.lordofthecraft.arche.save.rows.skin;

import net.lordofthecraft.arche.save.rows.SingleStatementRow;
import net.lordofthecraft.arche.skin.ArcheSkin;

import java.sql.Timestamp;
import java.util.UUID;

public class UpdateSkinRow extends SingleStatementRow {
    private final UUID player;
    private final int slot;
    private final String skinValue, skinSignature;
    private final Timestamp refresh;

    public UpdateSkinRow(ArcheSkin skin) {
        this.player = skin.getOwner();
        this.slot = skin.getIndex();

        this.skinValue = skin.getMojangProperty().getValue();
        this.skinSignature = skin.getMojangProperty().getSignature();
        this.refresh = skin.getLastRefreshed();
    }

    @Override
    protected String getStatement() {
        return "UPDATE persona_skins SET skinValue=?, skinSignature=?, refresh=? WHERE player=? AND slot=?";
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1:
                return skinValue;
            case 2:
                return skinSignature;
            case 3:
                return refresh;

            case 4:
                return player;
            case 5:
                return slot;
            default:
                throw new IllegalArgumentException();
        }
    }

}
