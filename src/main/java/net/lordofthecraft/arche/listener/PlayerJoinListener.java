package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {
	private final ArchePersonaHandler handler;
    private final ArcheTimer timer;

    public PlayerJoinListener(ArchePersonaHandler handler){
		this.handler = handler;
        timer = ArcheCore.getPlugin().getMethodTimer();
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() == Result.ALLOWED)
            handler.loadPlayer(e.getUniqueId(), e.getName());
    }
    
	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();
        if (timer != null) timer.startTiming("login " + p.getName());
        handler.joinPlayer(p);
        if (timer != null) timer.stopTiming("login" + p.getName());
        if (ArcheCore.getPlugin().debugMode())
            ArcheCore.getPlugin().getLogger().info("{Login} Currently have " + handler.getPersonas().size() + " persona files for " + Bukkit.getOnlinePlayers().size() + " players.");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLeave(PlayerQuitEvent e) {
        if (timer != null) timer.startTiming("logout");

		Player p = e.getPlayer();
        handler.leavePlayer(p);

        if(timer != null){
			timer.stopTiming("logout");
            int size = handler.getPersonas().size();
            ArcheCore.getPlugin().getLogger().info("[Debug] Seen Personas of " + size + " players at logout of " + p.getName());
        }
    }
}
