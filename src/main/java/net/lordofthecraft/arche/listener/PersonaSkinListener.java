package net.lordofthecraft.arche.listener;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.google.common.collect.Lists;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheGameProfile;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.skin.ArcheSkin;
import net.lordofthecraft.arche.skin.SkinCache;

public class PersonaSkinListener /*extends PacketAdapter*/ implements Listener {

	ProtocolManager manager = ProtocolLibrary.getProtocolManager();
	PersonaHandler handler = ArcheCore.getControls().getPersonaHandler();

	public void listen(){
		manager.addPacketListener(
				new PacketAdapter(ArcheCore.getPlugin(), ListenerPriority.LOWEST, PacketType.Play.Server.PLAYER_INFO) {
					@Override
					public void onPacketSending(PacketEvent event) {
						PacketContainer packet = event.getPacket();
						PlayerInfoAction at = packet.getPlayerInfoAction().read(0);
						
						if (at == PlayerInfoAction.ADD_PLAYER) {
							List<PlayerInfoData> pidl = packet.getPlayerInfoDataLists().read(0);
							List<PlayerInfoData> pidl_new = Lists.newArrayList();
							
							for(PlayerInfoData pid : pidl) {
								UUID uuid = pid.getProfile().getUUID();
								Player subject = Bukkit.getPlayer(uuid);
								if(subject != null) {
									Persona ps = handler.getPersona(subject);
									if(ps != null) {
										SkinCache cache = ArcheCore.getControls().getSkinCache();
										ArcheSkin skin = cache.getSkinFor(ps);	
										if(skin != null) {
											WrappedGameProfile reskinnedProfile = 
													ArcheGameProfile.reskin(pid.getProfile(), skin.getMojangSkinData());
											PlayerInfoData pid_new = new PlayerInfoData(reskinnedProfile,
													pid.getLatency(),
													pid.getGameMode(),
													pid.getDisplayName());
											pidl_new.add(pid_new);
											continue;
										}
									}
								}
								pidl_new.add(pid);
							}
						packet.getPlayerInfoDataLists().write(0, pidl_new);
						}
					}
				});
	}
	
	/*public static final Supplier<List<WrappedSignedProperty>> ARRAY_LIST_SUPPLIER = new Supplier<List<WrappedSignedProperty>>() {
		@Override
		public List<WrappedSignedProperty> get() {
			return Lists.newArrayList();
		}
	};
	
	public PersonaSkinListener(ArcheCore plugin) {
		super(new AdapterParameteters()
			.plugin(plugin)
			.gamePhase(GamePhase.BOTH)
			.serverSide()
			.optionAsync()
			.listenerPriority(ListenerPriority.HIGHEST)
			.types(PacketType.Play.Server.PLAYER_INFO));
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	private final static Table<UUID, UUID, WrappedChatComponent> displayNameCache = HashBasedTable.create();
	private final static ReentrantLock displayNameCacheLock = new ReentrantLock();

	//private final Map<UUID, WrappedSignedProperty> originSkinInfo = new HashMap<>();

	@Override
	public void onPacketSending(PacketEvent event) {
		if (event.getPacketType() != PacketType.Play.Server.PLAYER_INFO) {
			return;
		}
		WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(event.getPacket());
		if (packet.getAction() == EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME) {
			for (PlayerInfoData pd : packet.getData()) {
				if (pd.getDisplayName() == null) {
					displayNameCacheLock.lock();
					displayNameCache.remove(event.getPacket(), pd.getProfile().getUUID());
					displayNameCacheLock.unlock();
				} else {
					displayNameCacheLock.lock();
					displayNameCache.put(event.getPlayer().getUniqueId(), pd.getProfile().getUUID(), pd.getDisplayName());
					displayNameCacheLock.unlock();
				}
			}
			return;
		} else if (packet.getAction() != EnumWrappers.PlayerInfoAction.ADD_PLAYER) {
			return;
		}
		try {
			List<PlayerInfoData> data = packet.getData();
			List<PlayerInfoData> ndata = new ArrayList<>(data.size());
			for (PlayerInfoData pd : data) {
				WrappedGameProfile profile = pd.getProfile();

				// iterate through properties safely (currently only textures property is where)
				// map for property replacements
				final Multimap<String, WrappedSignedProperty> originProperties = profile.getProperties();
				Multimap<String, WrappedSignedProperty> signedPropertyMap = Multimaps.newListMultimap(
					Maps.<String, Collection<WrappedSignedProperty>>
						newHashMapWithExpectedSize(originProperties.size()),
					ARRAY_LIST_SUPPLIER);
				for (Map.Entry<String, WrappedSignedProperty> entry : originProperties.entries()) {
					if (entry.getKey().equalsIgnoreCase("textures")) {
						
							PersonaSkin skin = 	ArcheCore.getControls().getPersonaHandler().getPersona(Bukkit.getPlayer(profile.getUUID())).getSkin();
							//ensure the skin exists
							if (skin != null) {
								//put value in replacement map
								signedPropertyMap.put(entry.getKey(), new WrappedSignedProperty(
									entry.getValue().getName(),
									skin.getData(),
									skin.getSignature()));
								continue;
							}
						
					}
					// if skin is ot changed, put old values in map
					signedPropertyMap.put(entry.getKey(), entry.getValue());
				}
				//if no texture data is where, create some and apply changed skin directly if happend
				if (signedPropertyMap.isEmpty()) {
						PersonaSkin skin = 	ArcheCore.getControls().getPersonaHandler().getPersona(Bukkit.getPlayer(profile.getUUID())).getSkin();
						if (skin != null) {


							signedPropertyMap.put("textures", new WrappedSignedProperty(

								"textures"
								,
								skin.getData()
								,
								skin.getSignature()));

						} else {
							// fill map up with old data if available & no new data is available
							if (originProperties.isEmpty()) {

							} else {
								signedPropertyMap.putAll(originProperties);
							}
						}
				}
				// apply changes from our map
				originProperties.clear();
				originProperties.putAll(signedPropertyMap);

				// ensure we don't change the display name of the specific user
				WrappedChatComponent displayName = pd.getDisplayName();
				if (displayName == null) {
					displayName = WrappedChatComponent.fromText(profile.getName());
				} else {
					displayNameCacheLock.lock();
					displayNameCache.put(event.getPlayer().getUniqueId(), profile.getUUID(), displayName);
					displayNameCacheLock.unlock();
				}
				// add player info data to new data list
				ndata.add(new PlayerInfoData(profile, pd.getPing(), pd.getGameMode(), displayName));
			}

			try {
				packet.setData(ndata);
			} catch (RuntimeException e) {
				// always set the packet if any changes worked
				event.setPacket(packet.getHandle());
				//throw exception again sothe detailed error message will appear
				throw e;
			}
			event.setPacket(packet.getHandle());
		} catch (Throwable t) {
			//detailed and fast error mesage (StringBuilder is used)
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (PlayerInfoData playerInfoData : packet.getData()) {
				if (first)
					first = false;
				else
					sb.append(", ");
				sb.append(playerInfoData.getProfile().getName());
			}
			ArcheCore.getPlugin().getLogger().severe("Could not transform player list packet for player " + event.getPlayer().getName() + " about player's " + sb.toString());
			t.printStackTrace();
		}
	}

	public static void updatePlayerSkin(final Player player) {
		// first remove player's of tablist so we can set skin data again
		try {
			WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo();
			packet.setAction(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
			WrappedGameProfile profile = new WrappedGameProfile(player.getUniqueId(), null);
			packet.setData(Lists.newArrayList(new PlayerInfoData(profile, 0, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(""))));
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				if (!p.spigot().getHiddenPlayers().contains(player)) {
					packet.sendPacket(p);
				}
			}
		} catch (Throwable t) {
			ArcheCore.getPlugin().getLogger().severe("Could not update player skin for player " + player.getName() + ": Sending remove old skin failed!");
			t.printStackTrace();
		}
		// when create new playerlistpacket containing all the data of the player
		try {
			WrapperPlayServerPlayerInfo basePacket = new WrapperPlayServerPlayerInfo();
			basePacket.setAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER);
			WrappedGameProfile profile = new WrappedGameProfile(player.getUniqueId(), player.getName());
			final EnumWrappers.NativeGameMode nativeGameMode = EnumWrappers.NativeGameMode.fromBukkit(player.getGameMode());

			// get ping via reflection
			int ping;
			Class<?> craftPlayer = player.getClass();
			try {
				Method getHandle = craftPlayer.getMethod("getHandle", (Class[]) null);
				Object entityPlayer = getHandle.invoke(player);
				Field pingField = entityPlayer.getClass().getField("ping");
				ping = (int) pingField.get(entityPlayer);
			} catch (Throwable t) {
				ArcheCore.getPlugin().getLogger().warning("Could not get ping of player " + player.getName() + ". Error:");
				t.printStackTrace();
				ping = 0;
			}
			// send to all players
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				// ... except the ones who are hidden from the player
				if (!p.canSee(player)) {
					WrappedChatComponent displayName = null;

					// look whether the display name was changed
					displayNameCacheLock.lock();
					if (displayNameCache.contains(p, player))
						displayName = displayNameCache.get(p, player);
					displayNameCacheLock.unlock();

					// we have to set the display name as protocollib does not like it if we leave it out (wiki.vg says the packet allowes leaving out)
					if (displayName == null) {
						displayName = WrappedChatComponent.fromText(player.getName());
					}

					//create and send the packet
					WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo(basePacket.getHandle());
					packet.setData(Collections.singletonList(new PlayerInfoData(profile, ping, nativeGameMode, displayName)));
					packet.sendPacket(p); //invoke our own packet handler as this does not set any skin-related data
				}
			}
		} catch (Throwable t) {
			ArcheCore.getPlugin().getLogger().severe("Could not update player skin for player " + player.getName() + ": Sending new skin data failed!");
			t.printStackTrace();
		}
		// TODO test whether we have to hide and show the player for the skin to update
		try {
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				if (!p.canSee(player)) {
					p.hidePlayer(player);
					p.showPlayer(player);
				}
			}
		} catch (IllegalStateException ex) {
			//in case spigot does not like async, just do it sync!
			Bukkit.getScheduler().runTask(ArcheCore.getPlugin(), new Runnable() {
				@Override
				public void run() {
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						if (!p.canSee(player)) {
							p.hidePlayer(player);
							p.showPlayer(player);
						}
					}
				}
			});
		}
	}

	private void removeCachedDisplayNames(UUID uuid) {
		displayNameCacheLock.lock();
		Map<UUID, WrappedChatComponent> row = new HashMap<>(displayNameCache.row(uuid));
		for (UUID shown : row.keySet()) {
			displayNameCache.remove(uuid, shown);
		}
		Map<UUID, WrappedChatComponent> column = new HashMap<>(displayNameCache.column(uuid));
		for (UUID shownTo : column.keySet()) {
			displayNameCache.remove(shownTo, uuid);
		}
		displayNameCacheLock.unlock();
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		removeCachedDisplayNames(event.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent event) {
		removeCachedDisplayNames(event.getPlayer().getUniqueId());
	}*/
	
}
