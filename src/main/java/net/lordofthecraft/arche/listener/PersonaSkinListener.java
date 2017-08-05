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

public class PersonaSkinListener{

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
}
