package net.lordofthecraft.arche.util;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.Difficulty;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;

public class ProtocolUtil {

	@SuppressWarnings("deprecation")
	public static void fakeRespawn(Player p, Environment env) {
		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		
		PacketContainer packet = manager.createPacket(PacketType.Play.Server.RESPAWN);
		packet.getIntegers().write(0, env.getId()); //Does matter
		packet.getDifficulties().write(0, Difficulty.valueOf(p.getWorld().getDifficulty().name()));//Doesnt matter
		packet.getGameModes().write(0, NativeGameMode.fromBukkit(p.getGameMode()));
		packet.getWorldTypeModifier().write(0, p.getWorld().getWorldType()); //Doesnt matter tbh
		
		
		Location location = p.getLocation();
        PacketContainer teleport = manager.createPacket(PacketType.Play.Server.POSITION);
        teleport.getModifier().writeDefaults();
        teleport.getDoubles().write(0, location.getX());
        teleport.getDoubles().write(1, location.getY());
        teleport.getDoubles().write(2, location.getZ());
        teleport.getFloat().write(0, location.getYaw());
        teleport.getFloat().write(1, location.getPitch());
        teleport.getIntegers().writeSafely(0, -99);
        
		try {
			manager.sendServerPacket(p, packet);
			manager.sendServerPacket(p, teleport);
			
			//Some wizardry here to make the right amount of hearts how up
			if(p.getGameMode() == GameMode.ADVENTURE || p.getGameMode() == GameMode.SURVIVAL) {
				boolean toggle = p.isHealthScaled();
				p.setHealthScaled(!toggle);
				p.setHealthScale(p.getHealthScale());
				p.setHealth(p.getHealth());
				p.setHealthScaled(toggle);
			}
			
			//Some wizardry here to prevent unintended speedhacking
			p.setWalkSpeed(p.getWalkSpeed());
			
			//Redraw inventory as assumed empty on respawn
			p.updateInventory();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
	
}
