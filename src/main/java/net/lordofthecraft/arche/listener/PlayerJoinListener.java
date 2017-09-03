package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.event.PersonaActivateEvent;
import net.lordofthecraft.arche.event.PersonaDeactivateEvent;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.RaceBonusHandler;
import net.lordofthecraft.arche.save.SaveHandler;
import net.lordofthecraft.arche.save.tasks.persona.UnloadTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {
	private final ArchePersonaHandler handler;
    private final SaveHandler buffer;
    private final ArcheTimer timer;

    public PlayerJoinListener(ArchePersonaHandler handler){
		this.handler = handler;
        this.buffer = SaveHandler.getInstance();
        timer = ArcheCore.getPlugin().getMethodTimer();
    }
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e){
		if (ArcheCore.getControls().getPersonaHandler().isPreloading()) {
            e.getPlayer().kickPlayer("ArcheCore is currently loading personas, please wait before logging in.");
            return;
        }
		if(timer != null) timer.startTiming("login");
		Player p = e.getPlayer();
		RaceBonusHandler.reset(p);
		handler.initPlayer(p);
		Persona ps = handler.getPersona(p);
		if(ps != null) Bukkit.getPluginManager().callEvent(new PersonaActivateEvent(ps, PersonaActivateEvent.Reason.LOGIN));
		//if(logger != null) logger.putPair(p.getUniqueId(), p.getName());
		if(timer != null) timer.stopTiming("login");
		if(ArcheCore.getPlugin().debugMode()) ArcheCore.getPlugin().getLogger().info("{Login} Currently have " + handler.getPersonas().size() + " persona files for " + Bukkit.getOnlinePlayers().size() + " players." );

	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onLeave(PlayerQuitEvent e){
		if(timer != null) timer.startTiming("logout");
		ArcheCore plug = ArcheCore.getPlugin();
		Player p = e.getPlayer();
		
		Persona ps = handler.getPersona(p);
		if(ps != null) Bukkit.getPluginManager().callEvent(new PersonaDeactivateEvent(ps, PersonaDeactivateEvent.Reason.LOGOUT));
		
		//If Personas are not cached, send a signal into the consumer queue
		//To unload the Personas as soon as all save operations are done
        if (!plug.willCachePersonas()) buffer.put(new UnloadTask(p));

		RaceBonusHandler.reset(p);
		
		//Stop dupe?
		p.saveData();
		
		if(timer != null){
			timer.stopTiming("logout");
			int size = plug.getPersonaHandler().getPersonas().size();
			plug.getLogger().info("[Debug] Seen Personas of " + size + " players at logout of "  + e.getPlayer().getName());
		}
	}
}
