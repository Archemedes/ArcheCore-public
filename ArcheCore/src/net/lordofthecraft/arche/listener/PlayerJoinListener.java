package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.save.*;
import net.lordofthecraft.arche.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import net.lordofthecraft.arche.interfaces.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import net.lordofthecraft.arche.event.*;
import net.lordofthecraft.arche.save.tasks.*;
import net.lordofthecraft.arche.persona.*;

public class PlayerJoinListener implements Listener
{
    private final ArchePersonaHandler handler;
    private final SaveHandler buffer;
    private final ArcheTimer timer;
    
    public PlayerJoinListener(final ArchePersonaHandler handler) {
        super();
        this.handler = handler;
        this.buffer = SaveHandler.getInstance();
        this.timer = ArcheCore.getPlugin().getMethodTimer();
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(final PlayerJoinEvent e) {
        if (this.timer != null) {
            this.timer.startTiming("login");
        }
        final Player p = e.getPlayer();
        this.handler.initPlayer(p);
        final Persona ps = this.handler.getPersona(p);
        if (ps != null) {
            Bukkit.getPluginManager().callEvent((Event)new PersonaActivateEvent(ps, PersonaActivateEvent.Reason.LOGIN));
        }
        if (this.timer != null) {
            this.timer.stopTiming("login");
        }
        if (ArcheCore.getPlugin().debugMode()) {
            ArcheCore.getPlugin().getLogger().info("{Login} Currently have " + this.handler.getPersonas().size() + " persona files for " + Bukkit.getOnlinePlayers().size() + " players.");
        }
    }
    
    @EventHandler
    public void onLeave(final PlayerQuitEvent e) {
        if (this.timer != null) {
            this.timer.startTiming("logout");
        }
        final ArcheCore plug = ArcheCore.getPlugin();
        final Player p = e.getPlayer();
        final Persona ps = this.handler.getPersona(p);
        if (ps != null) {
            Bukkit.getPluginManager().callEvent((Event)new PersonaDeactivateEvent(ps, PersonaDeactivateEvent.Reason.LOGOUT));
        }
        if (!plug.willCachePersonas()) {
            this.buffer.put(new UnloadTask(p));
        }
        RaceBonusHandler.reset(p);
        if (this.timer != null) {
            this.timer.stopTiming("logout");
            final int size = plug.getPersonaHandler().getPersonas().size();
            plug.getLogger().info("[Debug] Seen Personas of " + size + " players at logout of " + e.getPlayer().getName());
        }
    }
}
