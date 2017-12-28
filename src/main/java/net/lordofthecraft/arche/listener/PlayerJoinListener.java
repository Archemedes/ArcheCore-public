package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.persona.ArcheOfflinePersona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;

import java.util.Collection;
import java.util.stream.Collectors;

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
		e.setJoinMessage(null);
		Player p = e.getPlayer();
		p.setExhaustion(3.5f);
        if (timer != null) timer.startTiming("login " + p.getName());
        if(!handler.getPersonaStore().isLoadedThisSession(p)) {
        	ArcheCore.getPlugin().getLogger().warning("Player " + p.getName() + " was NOT loaded Async. This is likely a late-bind issue");
        	handler.loadPlayer(p.getUniqueId(), p.getName());
        }
        handler.joinPlayer(p);
        if (timer != null) timer.stopTiming("login " + p.getName());
        if (ArcheCore.getPlugin().debugMode()) {
			Collection<ArcheOfflinePersona> personas = handler.getPersonas();
			int offlines = personas.stream().filter(ps -> ps.getClass() == ArcheOfflinePersona.class).collect(Collectors.toList()).size();
			int onlines = personas.stream().filter(ps -> ps.getClass() == ArchePersona.class).collect(Collectors.toList()).size();
			int playerCount = handler.getPersonaStore().getOnlineImplementedPersonas().size();
			ArcheCore.getPlugin().getLogger().info("[Login] " + personas.size() + " persona files (" + offlines + " offl. / " + onlines + "onl.) for "+Bukkit.getOnlinePlayers().size()+" (" + playerCount + ") players.");
		}
            
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onLeave(PlayerQuitEvent e) {
    	e.setQuitMessage(null);
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
