package net.lordofthecraft.arche.skin;

import com.mojang.authlib.properties.Property;
import net.lordofthecraft.arche.ArcheCore;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.Callable;

public class ArcheSkinCallable implements Callable<ArcheSkin> {

    private final int index;
    private final UUID owner;
    private final String name;
    private final String skinUrl;
    private final boolean slim;
    private final Property textures;
    private final Timestamp timeLastRefreshed;

    public ArcheSkinCallable(int index, UUID owner, String name, String skinUrl, boolean slim, Property textures, Timestamp timeLastRefreshed) {
        this.index = index;
        this.owner = owner;
        this.name = name;
        this.skinUrl = skinUrl;
        this.slim = slim;
        this.textures = textures;
        this.timeLastRefreshed = timeLastRefreshed;
    }

    @Override
    public ArcheSkin call() throws Exception {
        Connection c = ArcheCore.getSQLControls().getConnection();
        PreparedStatement stat = c.prepareStatement("INSERT INTO persona_skins (player,slot,name,skinUrl,slim,skinValue,skinSignature,refresh) VALUES (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
        stat.setString(1, owner.toString());
        stat.setInt(2, index);
        stat.setString(3, name);
        stat.setString(4, skinUrl);
        stat.setBoolean(5, slim);
        stat.setString(6, textures.getValue());
        stat.setString(7, textures.getSignature());
        stat.setTimestamp(8, timeLastRefreshed);
        stat.executeUpdate();
        ResultSet rs = stat.getGeneratedKeys();
        if (rs.next()) {
            int skinId = rs.getInt(1);
            return new ArcheSkin(skinId, owner, index, skinUrl, slim);
        }
        return null;
    }
}
