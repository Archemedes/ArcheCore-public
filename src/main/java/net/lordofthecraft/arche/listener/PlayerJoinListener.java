package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.lordofthecraft.arche.event.persona.PersonaActivateEvent;
import net.lordofthecraft.arche.event.persona.PersonaDeactivateEvent;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;
import net.lordofthecraft.arche.persona.RaceBonusHandler;
import net.lordofthecraft.arche.save.archerows.player.PlayerInsertRow;
import net.lordofthecraft.arche.save.archerows.player.UpdatePlayerRow;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {
	private final ArchePersonaHandler handler;
    private final ArcheTimer timer;

    public PlayerJoinListener(ArchePersonaHandler handler){
		this.handler = handler;
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
		if(ps != null) {
			Bukkit.getPluginManager().callEvent(new PersonaActivateEvent(ps, PersonaActivateEvent.Reason.LOGIN));
			ps.attributes().handleLogin();
            ArcheCore.getConsumerControls().queueRow(new UpdatePlayerRow(p));
        }else {
            //SaveHandler.getInstance().put(new PlayerRegisterTask(p.getUniqueId()));
            ArcheCore.getConsumerControls().queueRow(new PlayerInsertRow(p));
        }
		
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

		RaceBonusHandler.reset(p);
        if (ps != null) ps.attributes().handleSwitch(true);

        //Stop dupe?
        p.saveData();
		
		if(timer != null){
			timer.stopTiming("logout");
			int size = plug.getPersonaHandler().getPersonas().size();
			plug.getLogger().info("[Debug] Seen Personas of " + size + " players at logout of "  + e.getPlayer().getName());
		}
	}
}
