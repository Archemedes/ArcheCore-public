package net.lordofthecraft.arche.event;

import org.bukkit.event.*;
import java.util.*;

public class AsyncPlayerUnloadEvent extends Event
{
    private static final HandlerList handlers;
    private final String playerName;
    private final UUID playerUUID;
    
    public AsyncPlayerUnloadEvent(final String playerName, final UUID uuid) {
        super();
        this.playerName = playerName;
        this.playerUUID = uuid;
    }
    
    public String getPlayerName() {
        return this.playerName;
    }
    
    public UUID getPlayerUUID() {
        return this.playerUUID;
    }
    
    public HandlerList getHandlers() {
        return AsyncPlayerUnloadEvent.handlers;
    }
    
    public static HandlerList getHandlerList() {
        return AsyncPlayerUnloadEvent.handlers;
    }
    
    static {
        handlers = new HandlerList();
    }
}
