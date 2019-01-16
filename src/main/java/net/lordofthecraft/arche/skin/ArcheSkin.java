package net.lordofthecraft.arche.skin;

import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.collect.Sets;

import co.lotc.core.bukkit.util.ItemUtil;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.save.rows.skin.DeleteSkinRow;
import net.lordofthecraft.arche.save.rows.skin.InsertSkinRow;
import net.lordofthecraft.arche.save.rows.skin.UpdateSkinRow;
import org.bukkit.inventory.ItemStack;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class ArcheSkin {
    private final int index;
    private final UUID owner; //Player this cached skin belongs to
    private String skinUrl; //We use mojang url to refresh the validated skin data.
    private final boolean slim;

    Timestamp timeLastRefreshed = new Timestamp(0); //We need this to make sure we refresh every 24 hrs.
    WrappedSignedProperty mojangSkinData; //This is valid data to add to GameProfile

    private String name;

    Set<Persona> inUse = Sets.newConcurrentHashSet();

    public ArcheSkin(UUID uuid, int index, String url, boolean isSlim) {
        this.index = index;
		this.skinUrl = url;
		this.slim = isSlim;
		this.owner = uuid;
	}

    public String getName() {
        return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getURL() {
		return skinUrl;
	}

    public Set<Persona> getPersonas() {
        return Collections.unmodifiableSet(inUse);
    }

    public UUID getOwner() {
        return owner;
	}
	
	public int getIndex() {
		return index;
	}
	
	public boolean isSlim() {
		return slim;
	}

    public Timestamp getLastRefreshed() {
        return timeLastRefreshed;
	}

    public WrappedSignedProperty getMojangProperty() {
        return this.mojangSkinData;
    }

    public void removeAllPersonas() {
        inUse.forEach(Persona::removeSkin);
        inUse = Sets.newConcurrentHashSet();
    }

    public void addPersona(Persona persona) {
        inUse.add(persona);
    }

    public void removePersona(Persona persona) {
        inUse.remove(persona);
    }

    public ItemStack getHeadItem() {
        return ItemUtil.getSkullFromTexture(getMojangProperty().getValue());
    }

    void insertSql() {
        ArcheCore.getConsumerControls().queueRow(new InsertSkinRow(this));
    }

    void updateSql() {
        ArcheCore.getConsumerControls().queueRow(new UpdateSkinRow(this));
    }

    void deleteSql() {
        ArcheCore.getConsumerControls().queueRow(new DeleteSkinRow(this));
    }

    public static ArcheSkin fromSQL(ResultSet res) throws SQLException {
        UUID owner = UUID.fromString(res.getString("player"));
        int index = res.getInt("slot");

        boolean slim = res.getBoolean("slim");
        String skinUrl = res.getString("skinUrl");

        ArcheSkin skin = new ArcheSkin(owner, index, skinUrl, slim);

        skin.name = res.getString("name");

        String value = res.getString("skinValue");
        String signature = res.getString("skinSignature");
        skin.mojangSkinData = new WrappedSignedProperty("textures", value, signature);
        skin.timeLastRefreshed = res.getTimestamp("refresh");

        return skin;
	}
	
}
