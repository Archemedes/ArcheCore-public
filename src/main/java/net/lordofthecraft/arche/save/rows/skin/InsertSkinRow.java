package net.lordofthecraft.arche.save.rows.skin;

import net.lordofthecraft.arche.save.rows.SingleStatementRow;
import net.lordofthecraft.arche.skin.ArcheSkin;

import java.sql.Timestamp;
import java.util.UUID;

public class InsertSkinRow extends SingleStatementRow {
    private final UUID player;
    private final int slot;
    private final String name;
    private final String skinUrl;
    private final boolean slim;
    private final String skinValue, skinSignature;
    private final Timestamp refresh;

    public InsertSkinRow(ArcheSkin skin) {
        this.player = skin.getOwner();
        this.slot = skin.getIndex();
        this.name = skin.getName();
        this.skinUrl = skin.getURL();
        this.slim = skin.isSlim();
        this.skinValue = skin.getMojangProperty().getValue();
        this.skinSignature = skin.getMojangProperty().getSignature();
        this.refresh = skin.getLastRefreshed();
    }

    @Override
    protected String getStatement() {
        return "INSERT INTO persona_skins(player,slot,name,skinUrl,slim,skinValue,skinSignature,refresh) VALUES (?,?,?,?,?,?,?,?)";
    }

    @Override
    protected Object getValueFor(int index) {
        switch (index) {
            case 1:
                return player;
            case 2:
                return slot;
            case 3:
                return name;
            case 4:
                return skinUrl;
            case 5:
                return slim;
            case 6:
                return skinValue;
            case 7:
                return skinSignature;
            case 8:
                return refresh;
            default:
                throw new IllegalArgumentException();
        }
    }

}
