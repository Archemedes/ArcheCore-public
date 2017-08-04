package net.lordofthecraft.arche.skin;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;

public class SkinCache {
	private static final SkinCache INSTANCE = new SkinCache();
	
	private Multimap<UUID, ArcheSkin> skinCache = HashMultimap.create();
	private Map<PersonaKey, ArcheSkin> applied = Maps.newHashMap();
	
	
	public static SkinCache getInstance() { return INSTANCE; }
	private SkinCache() {}
	
	private ArcheSkin savePlayerSkin(Player p, int index) throws UnsupportedEncodingException, ParseException {
		WrappedGameProfile profile = WrappedGameProfile.fromPlayer(p); //Protocollib for version independence	}
		Multimap<String, WrappedSignedProperty> properties = profile.getProperties();
		String encodedValue = properties.get("textures").stream() //Should be only 1
		.map( x -> x.getValue())
		.findFirst()
		.get();
		
		String actualValue = new String(Base64.decodeBase64(encodedValue), "UTF-8");
		JSONParser parser = new JSONParser();
		JSONObject topJson = (JSONObject) parser.parse(actualValue);
		JSONObject textureJson = (JSONObject) topJson.get("textures");
		JSONObject skinJson = (JSONObject) textureJson.get("SKIN");
		Object metadata = skinJson.get("metadata");
		boolean slim = metadata != null;
		String skinUrl = skinJson.get("url").toString();
		
		ArcheSkin skin = new ArcheSkin(index, skinUrl, slim);
		skin.timeLastRefreshed = System.currentTimeMillis(); //Actually refreshed during the player's login.
		skin.mojangSkinData = ((GameProfile) profile.getHandle()).getProperties();
		
		return skin;
	}
	
	public void storeSkin(Player p, int index) throws UnsupportedEncodingException, ParseException {
		ArcheSkin skin = savePlayerSkin(p, index);
		Iterator<ArcheSkin> skins = skinCache.get(p.getUniqueId()).iterator();
		while(skins.hasNext()) {
			ArcheSkin remove = skins.next();
			if(remove.getIndex() == index) {
				skins.remove();
				break;
			}
		}
		
		skin.insertSql();
		skinCache.put(p.getUniqueId(), skin);
		
	}
	
	public ArcheSkin getSkinFor(Persona pers) {
		return applied.get(pers.getPersonaKey());
	}
	
		
	
}