package net.lordofthecraft.arche.interfaces;

import java.util.List;

/**
 * a simple account which can have multiple Minecraft characters
 */
public interface Account {

	List<Toon> getToons();
	
	Tags<Account> getTags();
	
	int getId();
}
