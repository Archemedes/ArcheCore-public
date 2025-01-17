package net.lordofthecraft.arche.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedSignedProperty;
import com.google.common.collect.Multimap;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.skin.ArcheSkin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

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
                            boolean changed = false;
                            List<PlayerInfoData> pidl = packet.getPlayerInfoDataLists().read(0);
                            
                            for(ListIterator<PlayerInfoData> it = pidl.listIterator(); it.hasNext(); ) {
                            	PlayerInfoData pid=it.next();
                            	UUID uuid = pid.getProfile().getUUID();
								Player subject = Bukkit.getPlayer(uuid);
								if(subject != null) {
									Persona ps = handler.getPersona(subject);
									if(ps != null) {
                                        ArcheSkin skin = ps.getSkin();
                                        if(skin != null) {
                                        	changed = true;
                                            WrappedGameProfile newProf = new WrappedGameProfile(uuid, subject.getName());
                                            Multimap<String, WrappedSignedProperty> properties = newProf.getProperties();
                                            properties.removeAll("textures");
                                            properties.put("textures", skin.getMojangProperty());
                                            pid = new PlayerInfoData(newProf, pid.getLatency(), pid.getGameMode(), pid.getDisplayName());
                                            it.set(pid);
                                        }
                                    }
								}
							}
                            if (changed) packet.getPlayerInfoDataLists().write(0, pidl);
                        }
                    }
				});
	}
}
