package net.lordofthecraft.arche.interfaces;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * a simple account which can have multiple Minecraft characters
 */
public interface Account {
	
	Tags<Account> getTags();
	
	int getId();
	
	void setForumId(long forumId);
	long getForumId();
	boolean hasForumId();
	
	void setDiscordId(long discordId);
	long getDiscordId();
	boolean hasDiscordId();
	
	long getTimePlayed();
	
	long getTimePlayedThisWeek();
	
	long getLastSeen();
	
	void addItemsToCache(List<ItemStack> items);
	
	void setItemCache(List<ItemStack> items);
		
	List<ItemStack> getItemCache();
	
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
