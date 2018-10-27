package net.lordofthecraft.arche.interfaces;

import java.util.UUID;

public interface Toon { //might get renamed

	Account getAccount();
	
	UUID getUniqueId();
	
	Tags<Toon> getTags();
}
