package net.lordofthecraft.arche.skin;

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
import com.google.common.collect.Multimap;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.listener.PersonaSkinListener;
import net.lordofthecraft.arche.skin.MojangCommunicator.AuthenthicationData;
import net.lordofthecraft.arche.skin.MojangCommunicator.AuthenticationException;
import net.lordofthecraft.arche.skin.MojangCommunicator.MinecraftAccount;
import net.lordofthecraft.arche.util.ProtocolUtil;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class SkinCache {
	private static final SkinCache INSTANCE = new SkinCache();
	
	private Multimap<UUID, ArcheSkin> skinCache = HashMultimap.create();

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
                .map(WrappedSignedProperty::getValue)
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
        skin.timeLastRefreshed = new Timestamp(0);
        skin.mojangSkinData = profile.getProperties().get("textures").iterator().next();

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
        pers.setSkin(skin);
        return true;
	}
	
	public ArcheSkin getSkinAtSlot(UUID playerUUID, int index) {
		return skinCache.get(playerUUID).stream()
		.filter(s -> s.getIndex() == index)
		.findAny().orElse(null);
	}

    private void init() {
        try {
            Connection conn = ArcheCore.getSQLControls().getConnection();
            conn.setReadOnly(true);
            PreparedStatement selectStatement = conn.prepareStatement("SELECT player,slot,name,skinUrl,slim,skinValue,skinSignature,refresh FROM persona_skins");
            ResultSet res = selectStatement.executeQuery();
			while(res.next()) {
				ArcheSkin skin = ArcheSkin.fromSQL(res);
				skinCache.put(skin.getOwner(), skin);
			}
            res.close();
            selectStatement.close();
            conn.close();
        }catch(SQLException e) {e.printStackTrace();}
	}
	
	public boolean removeSkin(UUID uuid, int index) {
		ArcheSkin sk = getSkinAtSlot(uuid, index);
		if(sk == null) return false;
        sk.removeAllPersonas();
        sk.deleteSql();
		skinCache.remove(uuid, sk);
		return true;
	}
	
	public boolean clearSkin(Persona ps) {
        if (!ps.hasSkin()) return false;

        ps.removeSkin();

        Player p = ps.getPlayer();
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
                .filter(s -> s.getLastRefreshed().getTime() < tooOldTime)
                .sorted((s1, s2) -> Long.compare(s2.getLastRefreshed().getTime(), s1.getLastRefreshed().getTime()))
                .findFirst().orElse( null);
		
		if(whichOneToRefresh == null) return null;
		AuthenthicationData auth = MojangCommunicator.authenthicate(account);
		MojangCommunicator.setSkin(auth, whichOneToRefresh.getURL());
        whichOneToRefresh.mojangSkinData = MojangCommunicator.requestSkin(auth.uuid);
        whichOneToRefresh.timeLastRefreshed = new Timestamp(System.currentTimeMillis());
        whichOneToRefresh.updateSql();
		
		return whichOneToRefresh;
	}


}