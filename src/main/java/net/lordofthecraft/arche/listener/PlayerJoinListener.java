package net.lordofthecraft.arche.listener;

import java.sql.Timestamp;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.event.persona.PersonaDeactivateEvent;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.RaceBonusHandler;
import net.lordofthecraft.arche.save.PersonaField;
import net.lordofthecraft.arche.save.rows.persona.update.PersonaUpdateRow;

public class PlayerJoinListener implements Listener {
	private final ArchePersonaHandler handler;
    private final ArcheTimer timer;

    public PlayerJoinListener(ArchePersonaHandler handler){
		this.handler = handler;
        timer = ArcheCore.getPlugin().getMethodTimer();
    }
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(AsyncPlayerPreLoginEvent e) {
		handler.loadPlayer(e.getUniqueId(), e.getName());
	}
    
	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e){
		Player p = e.getPlayer();
		if(timer != null) timer.startTiming("login " + p.getName());
		handler.joinPlayer(p);		
		if(timer != null) timer.stopTiming("login" + p.getName());
		if(ArcheCore.getPlugin().debugMode()) ArcheCore.getPlugin().getLogger().info("{Login} Currently have " + handler.getPersonas().size() + " persona files for " + Bukkit.getOnlinePlayers().size() + " players." );
	}
	
	@EventHandler (priority = EventPriority.LOW)
	public void onLeave(PlayerQuitEvent e){
		if(timer != null) timer.startTiming("logout");
		ArcheCore plug = ArcheCore.getPlugin();
		Player p = e.getPlayer();
		
		Persona ps = handler.getPersona(p);
		if(ps != null) Bukkit.getPluginManager().callEvent(new PersonaDeactivateEvent(ps, PersonaDeactivateEvent.Reason.LOGOUT));

		RaceBonusHandler.reset(p);
        if (ps != null) ps.attributes().handleSwitch(true);

        //Stop dupe?
        p.saveData();

        ArcheCore.getConsumerControls().queueRow(new PersonaUpdateRow(ps, PersonaField.STAT_LAST_PLAYED, new Timestamp(System.currentTimeMillis()), false));

        if(timer != null){
			timer.stopTiming("logout");
			int size = plug.getPersonaHandler().getPersonas().size();
			plug.getLogger().info("[Debug] Seen Personas of " + size + " players at logout of "  + e.getPlayer().getName());
		}
	}
}
