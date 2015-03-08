package net.lordofthecraft.arche.menu;

import java.util.Map;

import org.bukkit.entity.Player;

public interface Menu {

	
	public Player getOwner();
	
	public Map<String, Object> getContext();
	
	
}
