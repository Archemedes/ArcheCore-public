package net.lordofthecraft.arche.skin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

public class ArcheSkin {
	private int index;
	private UUID owner; //Player this cached skin belongs to
	private String skinUrl; //We use mojang url to refresh the validated skin data.
	private boolean slim;
	
	private long timeLastRefreshed; //We need this to make sure we refresh every 24 hrs.
	private PropertyMap mojangSkinData; //This is valid data to add to GameProfile
	
	public ArcheSkin(int index, String url, boolean isSlim) {
		this.index = index;
		this.skinUrl = url;
		this.slim = isSlim;
	}
	
	private ArcheSkin() {};
	
	public String getURL() {
		return skinUrl;
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
