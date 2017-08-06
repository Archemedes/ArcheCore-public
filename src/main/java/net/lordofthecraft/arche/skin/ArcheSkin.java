package net.lordofthecraft.arche.skin;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
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
	
	String name;
	
	public ArcheSkin(UUID uuid, int index, String url, boolean isSlim) {
		this.index = index;
		this.skinUrl = url;
		this.slim = isSlim;
		this.owner = uuid;
	}
	
	private ArcheSkin() {};
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
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
	
	public ItemStack getHeadItem(){ //Kowaman
		ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
		//return skull;
		ItemMeta skullMeta = skull.getItemMeta();
		
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);		
		profile.getProperties().putAll("textures", mojangSkinData.get("textures"));
		
		Field profileField = null;
		
		try {
			profileField = skullMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(skullMeta, profile);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		}
		skull.setItemMeta(skullMeta);
		return skull;
	}
	
	
	void insertSql() {
		Map<String, Object> toIn = Maps.newLinkedHashMap();
		toIn.put("player", this.owner);
		toIn.put("slot", this.index);
		toIn.put("name", this.name);
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
	
	void deleteSql() {
		Map<String, Object> crit= Maps.newLinkedHashMap();
		crit.put("player", this.owner);
		crit.put("slot", this.index);
		ArcheCore.getControls().getSQLHandler().remove("persona_skins", crit);
	}
	
	public static ArcheSkin fromSQL(ResultSet res) throws SQLException {
		ArcheSkin skin = new ArcheSkin();
		skin.owner = UUID.fromString(res.getString(1));
		skin.index = res.getInt(2);
		skin.name = res.getString(3);
		skin.skinUrl = res.getString(4);
		skin.slim = res.getInt(5) != 0;
		
		String value = res.getString(6);
		String signature = res.getString(7);
		skin.mojangSkinData = new PropertyMap();
		skin.mojangSkinData.put("textures", new Property("textures", value, signature));
		
		skin.timeLastRefreshed = res.getLong(8);
		
		return skin;
	}
	
}
