package net.lordofthecraft.arche.skin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import net.lordofthecraft.arche.ArcheCore;

public class ArcheSkin {
	private int index;
	private UUID owner; //Player this cached skin belongs to
	private String skinUrl; //We use mojang url to refresh the validated skin data.
	private boolean slim;
	
	long timeLastRefreshed; //We need this to make sure we refresh every 24 hrs.
	PropertyMap mojangSkinData; //This is valid data to add to GameProfile
	
	public ArcheSkin(UUID uuid, int index, String url, boolean isSlim) {
		this.index = index;
		this.skinUrl = url;
		this.slim = isSlim;
		this.owner = uuid;
	}
	
	private ArcheSkin() {};
	
	public String getURL() {
		return skinUrl;
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
	
	public long getLastRefreshed() {
		return timeLastRefreshed;
	}
	
	public PropertyMap getMojangSkinData() {
		return mojangSkinData;
	}
	
	private Property getProperty() {
		return this.mojangSkinData.get("textures").iterator().next();	
	}
	
	void insertSql() {
		Map<String, Object> toIn = Maps.newLinkedHashMap();
		toIn.put("player", this.owner);
		toIn.put("slot", this.index);
		toIn.put("skinUrl", this.skinUrl);
		toIn.put("slim", slim? 1:0);
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
	
	public static ArcheSkin fromSQL(ResultSet res) throws SQLException {
		ArcheSkin skin = new ArcheSkin();
		skin.owner = UUID.fromString(res.getString(1));
		skin.index = res.getInt(2);
		skin.skinUrl = res.getString(3);
		skin.slim = res.getInt(4) != 0;
		
		String value = res.getString(5);
		String signature = res.getString(6);
		skin.mojangSkinData = new PropertyMap();
		skin.mojangSkinData.put("textures", new Property("textures", value, signature));
		
		skin.timeLastRefreshed = res.getLong(7);
		
		return skin;
	}
	
}
