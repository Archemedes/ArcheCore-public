package net.lordofthecraft.arche.skin;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.util.ItemUtil;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ArcheSkin {
    private final int skin_id;
    private int index;
    private UUID owner; //Player this cached skin belongs to
    private String skinUrl; //We use mojang url to refresh the validated skin data.
    private boolean slim;

    Timestamp timeLastRefreshed; //We need this to make sure we refresh every 24 hrs.
    PropertyMap mojangSkinData; //This is valid data to add to GameProfile
	
	String name;

    Set<Persona> inUse = Sets.newConcurrentHashSet();

    public ArcheSkin(int skin_id, UUID uuid, int index, String url, boolean isSlim) {
        this.skin_id = skin_id;
        this.index = index;
		this.skinUrl = url;
		this.slim = isSlim;
		this.owner = uuid;
	}

    protected ArcheSkin(int skin_id) {
        this.skin_id = skin_id;
    }

    public int getSkinId() {
        return skin_id;
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
	
	public PropertyMap getMojangSkinData() {
		return mojangSkinData;
	}
	
	private Property getProperty() {
		return this.mojangSkinData.get("textures").iterator().next();	
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
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);		
		profile.getProperties().putAll("textures", mojangSkinData.get("textures"));
		
		return ItemUtil.getSkullFromTexture(mojangSkinData.get("textures").iterator().next().getValue());
	}
	
	
	void insertSql() {
		Map<String, Object> toIn = Maps.newLinkedHashMap();
		toIn.put("player", this.owner);
		toIn.put("slot", this.index);
		toIn.put("name", this.name);
		toIn.put("skinUrl", this.skinUrl);
        toIn.put("slim", slim);
        Property textures = getProperty();
		toIn.put("skinValue", textures.getValue());
		toIn.put("skinSignature", textures.getSignature());
		toIn.put("refresh", this.timeLastRefreshed);
		ArcheCore.getControls().getSQLHandler().insert("persona_skins", toIn);
	}
	
	void updateSql() {
		Map<String, Object> toIn= Maps.newLinkedHashMap();
		Property textures = getProperty();
		toIn.put("skinValue", textures.getValue());
		toIn.put("skinSignature", textures.getSignature());
		toIn.put("refresh", this.timeLastRefreshed);
		
		Map<String, Object> crit= Maps.newLinkedHashMap();
		crit.put("player", this.owner);
		crit.put("slot", this.index);
		ArcheCore.getControls().getSQLHandler().update("persona_skins", toIn, crit);
	}
	
	void deleteSql() {
		Map<String, Object> crit= Maps.newLinkedHashMap();
		crit.put("player", this.owner);
		crit.put("slot", this.index);
		ArcheCore.getControls().getSQLHandler().remove("persona_skins", crit);
	}


    /*
        protected static void createPersonaSkinsTable(SQLHandler sqlHandler) {
        //Skins table
        Map<String,String> 	cols = Maps.newLinkedHashMap();
        cols.put("player", "TEXT NOT NULL");
        cols.put("slot", "INT");
        cols.put("name", "TEXT");
        cols.put("skinUrl", "TEXT");
        cols.put("slim", "INT");
        cols.put("skinValue", "TEXT");
        cols.put("skinSignature", "TEXT");
        cols.put("refresh", "INT");
        cols.put("PRIMARY KEY (player, slot)", "");
        sqlHandler.createTable("persona_skins", cols);

        cols = Maps.newLinkedHashMap();
        cols.put("player", "TEXT NOT NULL");
        cols.put("id", "INT NOT NULL");
        cols.put("slot", "INT");
        cols.put("PRIMARY KEY (player, id)", "");
        sqlHandler.createTable("persona_skins_used", cols);

    }
     */
    public static ArcheSkin fromSQL(ResultSet res) throws SQLException {
        ArcheSkin skin = new ArcheSkin(res.getInt("skin_id"));
        skin.owner = UUID.fromString(res.getString("player"));
        skin.index = res.getInt("slot");
        skin.name = res.getString("name");
        skin.skinUrl = res.getString("skinUrl");
        skin.slim = res.getBoolean("slim");

        String value = res.getString("skinValue");
        String signature = res.getString("skinSignature");
        skin.mojangSkinData = new PropertyMap();
		skin.mojangSkinData.put("textures", new Property("textures", value, signature));

        skin.timeLastRefreshed = res.getTimestamp("refresh");

        return skin;
	}
	
}
