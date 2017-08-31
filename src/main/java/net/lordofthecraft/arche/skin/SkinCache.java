package net.lordofthecraft.arche.skin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.properties.PropertyMap;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import net.lordofthecraft.arche.listener.PersonaSkinListener;
import net.lordofthecraft.arche.persona.ArchePersonaKey;
import net.lordofthecraft.arche.skin.MojangCommunicator.AuthenthicationData;
import net.lordofthecraft.arche.skin.MojangCommunicator.MinecraftAccount;
import net.lordofthecraft.arche.util.ProtocolUtil;

public class SkinCache {
	private static final SkinCache INSTANCE = new SkinCache();
	
	private Multimap<UUID, ArcheSkin> skinCache = HashMultimap.create();
	private Map<PersonaKey, ArcheSkin> applied = Maps.newHashMap(); //TODO add as a field of Persona and update SQL?
	
	//private final SkinRefresher refreshTask;
	
	public static SkinCache getInstance() { return INSTANCE; }
	private SkinCache() { 
		init();
		new PersonaSkinListener().listen(); 
		
		//Note: we turn this off as there doesnt seem to be an expiry date
		//on signed skin data sent by Mojang right now. Why? IDK. But rejoice.
		/*refreshTask = new SkinRefresher(this);
		refreshTask.runTaskTimerAsynchronously(ArcheCore.getPlugin(), 20*120, 20*120);*/
	}
	
	private int refreshThresholdInHours = 15;
	
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
		@SuppressWarnings("unchecked")
		Object metadata = skinJson.getOrDefault("metadata", null);
		boolean slim = metadata != null;
		String skinUrl = skinJson.get("url").toString();
		
		ArcheSkin skin = new ArcheSkin(p.getUniqueId(), index, skinUrl, slim);
		skin.timeLastRefreshed = 0;
		skin.mojangSkinData = ((GameProfile) profile.getHandle()).getProperties();
		
		return skin;
	}
	
	public int storeSkin(Player p, int index, String name) throws UnsupportedEncodingException, ParseException {
		ArcheSkin skin = savePlayerSkin(p, index);
		
		for(ArcheSkin sk : skinCache.get(p.getUniqueId())) {
			if(skin.getURL().equals(sk.getURL())){
				return sk.getIndex();
			}
		}
		
		
		skin.setName(name);
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
		return -1;
	}
	
	public boolean applySkin(Persona pers, int index) {
		UUID uuid = pers.getPlayerUUID();
		ArcheSkin skin = skinCache.get(uuid).stream()
		.filter(ss -> ss.getIndex() == index)
		.findAny().orElse(null);
		
		if(skin == null) return false;
		applied.put(pers.getPersonaKey(), skin);
		Map<String, Object> toIn = Maps.newLinkedHashMap();
		toIn.put("player", pers.getPlayerUUID());
		toIn.put("id", pers.getId());
		toIn.put("slot", index);
		ArcheCore.getControls().getSQLHandler().insert("persona_skins_used", toIn);
		return true;
	}
	
	
	public ArcheSkin getSkinFor(Persona pers) {
		if(pers == null) return null;
		return applied.get(pers.getPersonaKey());
	}
	
	public ArcheSkin getSkinAtSlot(UUID playerUUID, int index) {
		return skinCache.get(playerUUID).stream()
		.filter(s -> s.getIndex() == index)
		.findAny().orElse(null);
	}
	
	private void init() {
		try {
			PreparedStatement selectStatement = ArcheCore.getControls().getSQLHandler().getConnection().prepareStatement("SELECT * FROM persona_skins");
			ResultSet res = selectStatement.executeQuery();
			while(res.next()) {
				ArcheSkin skin = ArcheSkin.fromSQL(res);
				skinCache.put(skin.getOwner(), skin);
			}

			selectStatement.close();
			selectStatement = ArcheCore.getControls().getSQLHandler().getConnection().prepareStatement("SELECT * FROM persona_skins_used");
			res = selectStatement.executeQuery();
			while(res.next()) {
				UUID uuid = UUID.fromString(res.getString(1));
				int pId = res.getInt(2);
				int index = res.getInt(3);
				ArcheSkin skin = getSkinAtSlot(uuid, index);
				if(skin != null) {
					PersonaKey key = new ArchePersonaKey(uuid, pId);
					applied.put(key, skin);
				} else {
					ArcheCore.getPlugin().getLogger().warning("Persona applied skin not found: "
							+ uuid + "," + pId + "," + index);
				}
			}

		}catch(SQLException e) {e.printStackTrace();}
	}
	
	public boolean removeSkin(UUID uuid, int index) {
		ArcheSkin sk = getSkinAtSlot(uuid, index);
		if(sk == null) return false;
		sk.deleteSql();
		skinCache.remove(uuid, sk);
		
		Iterator<ArcheSkin> iter = applied.values().iterator();
		while(iter.hasNext()) {
			ArcheSkin other = iter.next();
			if(sk == other) iter.remove();
		}
		
		Map<String, Object> crit= Maps.newLinkedHashMap();
		crit.put("player", uuid.toString());
		crit.put("slot", index);
		ArcheCore.getControls().getSQLHandler().remove("persona_skins_used", crit);
		
		return true;
	}
	
	public boolean clearSkin(Persona ps) {
		PersonaKey key = ps.getPersonaKey();
		
		ArcheSkin sk = applied.remove(key);
		if(sk == null) return false;

		Map<String, Object> crit= Maps.newLinkedHashMap();
		crit.put("player", key.getPlayerUUID().toString());
		crit.put("id", key.getPersonaId());
		crit.put("slot", sk.getIndex());
		ArcheCore.getControls().getSQLHandler().remove("persona_skins_used", crit);
		
		Player p = Bukkit.getPlayer(key.getPlayerUUID());
		if(p != null && ps.isCurrent()) refreshPlayer(p);
		
		return true;
	}
	
	public void refreshPlayer(Player p) {
		Bukkit.getOnlinePlayers().stream()
		.filter(x -> (x != p))
		.filter(x -> x.canSee(p))
		.forEach(x -> {x.hidePlayer(p); x.showPlayer(p);});
		
		
		final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		
		WrappedGameProfile profile = WrappedGameProfile.fromPlayer(p); //Protocollib for version independence
		List<PlayerInfoData> lpid = Lists.newArrayList();
		
		lpid.add(new PlayerInfoData(profile, 
				1, //who cares honestly
				NativeGameMode.fromBukkit(p.getGameMode()), 
				WrappedChatComponent.fromText(p.getDisplayName())));
		
		final PacketContainer packetDel = manager.createPacket(PacketType.Play.Server.PLAYER_INFO);
		final PacketContainer packetAdd = manager.createPacket(PacketType.Play.Server.PLAYER_INFO);
		packetDel.getPlayerInfoAction().write(0, PlayerInfoAction.REMOVE_PLAYER);
		packetAdd.getPlayerInfoAction().write(0, PlayerInfoAction.ADD_PLAYER);
		packetDel.getPlayerInfoDataLists().write(0, lpid);
		packetAdd.getPlayerInfoDataLists().write(0, lpid);
		
		try {
			manager.sendServerPacket(p, packetDel);
			manager.sendServerPacket(p, packetAdd);
			ProtocolUtil.fakeRespawn(p, p.getWorld().getEnvironment());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
	
	/*---------------------- Tasks used for the Runnable ----------------------*/
	
	int checkRefreshTime(int accs) {
		//Assume we refresh 25 skins per hour (we do 1 per 2 minutes, plus downtime, crashes)
		//Skin cache size / 25 = hours in advance we need to refresh all skins.
		
		int cachedSkins = skinCache.values().size();
		refreshThresholdInHours = 2 + (cachedSkins / (25*accs));
		return refreshThresholdInHours;
	}
	
	ArcheSkin grabOneSkinAndRefresh(MinecraftAccount account) throws IOException, ParseException, AuthenticationException {
		//Run this Async. Obviously.
		long tooOldTime = System.currentTimeMillis() - (this.refreshThresholdInHours*3600*1000);
		
		ArcheSkin whichOneToRefresh = skinCache.values().stream()
			.filter(s -> s.getLastRefreshed() < tooOldTime )
			.sorted((s1,s2) -> Long.compare(s2.getLastRefreshed(), s1.getLastRefreshed()) )
			.findFirst().orElse( null);
		
		if(whichOneToRefresh == null) return null;
		AuthenthicationData auth = MojangCommunicator.authenthicate(account);
		MojangCommunicator.setSkin(auth, whichOneToRefresh.getURL());
		PropertyMap props = MojangCommunicator.requestSkin(auth.uuid);
		whichOneToRefresh.mojangSkinData = props;
		whichOneToRefresh.timeLastRefreshed = System.currentTimeMillis();
		whichOneToRefresh.updateSql();
		
		return whichOneToRefresh;
	}


}