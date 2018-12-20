package net.lordofthecraft.arche.interfaces;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

/**
 * a simple account which can have multiple Minecraft characters
 */
public interface Account {
	
	Tags<Account> getTags();
	
	int getId();
	
	long getForumId();
	
	long getDiscordId();
	
	long getTimePlayed();
	
	long getTimePlayedThisWeek();
	
	long getLastSeen();
	
	/**
	 * @return Name of the online player, or a best-guess by randomly picking among alt uuids.
	 */
	String getName();
	
	Player getPlayer();
	
	List<Persona> getPersonas();
	
	List<String> getUsernames();
	
	Set<UUID> getUUIDs();
	
	Set<String> getIPs();
}
