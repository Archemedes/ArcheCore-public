package net.lordofthecraft.arche.interfaces;

import java.util.UUID;

/**
 * a simple account which can have multiple Minecraft characters
 */
public interface Account {

	UUID getUniqueId();
	
	Tags<Account> getTags();
	
	int getId();
	
	long getForumId();
	
	long getDiscordId();
}
