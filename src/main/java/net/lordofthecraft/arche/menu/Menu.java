package net.lordofthecraft.arche.menu;

import org.bukkit.entity.Player;

import java.util.Map;

public interface Menu {


	Player getOwner();

	Map<String, Object> getContext();
	
	
}
