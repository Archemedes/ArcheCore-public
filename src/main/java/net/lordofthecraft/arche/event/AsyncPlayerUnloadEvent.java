package net.lordofthecraft.arche.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Called when the ArcheCore SQLite database has successfully processes all
 * ArcheTasks up until the Player has logged out. This generally means that loading tables
 * from the SQLite database can now be done without risking inconsistencies. 
 */
public class AsyncPlayerUnloadEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private final String playerName;
	private final UUID playerUUID;
	
	public AsyncPlayerUnloadEvent(String playerName, UUID uuid) {
		super();
		this.playerName = playerName;
		this.playerUUID = uuid;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	/**
	 * Get the known name of the Player that was unloaded
	 * @return the unloaded Player's name.
	 */
	public String getPlayerName(){
		return playerName;
	}
	
	public UUID getPlayerUUID(){
		return playerUUID;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	

}
