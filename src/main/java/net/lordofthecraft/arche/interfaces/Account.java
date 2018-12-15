package net.lordofthecraft.arche.interfaces;

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
	
	Player getPlayer();
	
	Set<UUID> getUUIDs();
	
	Set<String> getIPs();
}
