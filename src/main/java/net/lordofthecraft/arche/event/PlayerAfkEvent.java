package net.lordofthecraft.arche.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerAfkEvent extends PlayerEvent {
	private final boolean isNowAfk;
	
	public PlayerAfkEvent(Player who, boolean afk) {
		super(who);
		isNowAfk = afk;
	}
	
	public boolean isAfk() {
		return isNowAfk;
	}
	
	//Boilerplate
	private static final HandlerList handlers = new HandlerList();
	public static HandlerList getHandlerList() {return handlers;}
	public HandlerList getHandlers() {return handlers;}
}
