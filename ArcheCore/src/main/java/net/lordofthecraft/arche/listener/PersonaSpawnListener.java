package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Respawns a persona at a location, if one has been specified.
 */
public class PersonaSpawnListener implements Listener {

    private final PersonaHandler handler;

    public PersonaSpawnListener(PersonaHandler handler) {
        this.handler = handler;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        if (handler.hasPersona(e.getPlayer())) {
            Persona pers = handler.getPersona(e.getPlayer());
            if (handler.getRacespawns().containsKey(pers.getRace())) {
                e.setRespawnLocation(handler.getRacespawns().get(pers.getRace()));
            }
        }
    }
}
