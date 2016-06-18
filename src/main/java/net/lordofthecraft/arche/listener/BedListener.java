package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.interfaces.PersonaHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

/**
 * Sleeping results in bonus experience for X total experience.
 * Modeled off of the World of Warcraft Tavern/City rest system.
 * <p>
 * The "Bonus Experience" is a fixed rate of increased experience gained until the user
 * has earned a set amount of experience. Basically, just getting to a certain threshhold
 * faster.
 * <p>
 * Feature Goals:
 * - Encourage players to spend more times in a home
 * - Encourage players to spend more time building a home
 * - Add additional professions catchup mechanics
 * - Make the bed great again
 * <p>
 * Thoughts:
 * - Too abusable?
 *
 * @author 501warhead
 */
public class BedListener implements Listener {

    private final PersonaHandler handler;
    //todo help file for bonus experience and/or beds

    public BedListener(PersonaHandler handler) {
        this.handler = handler;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerSleep(PlayerBedEnterEvent e) {
        if (handler.hasPersona(e.getPlayer())) {
            //code
        }
        //todo shh... go to sleep. Wake up strong and rested.
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerWake(PlayerBedLeaveEvent e) {
        if (handler.hasPersona(e.getPlayer())) {
            //code
        }
        //todo Rise and shine! No more bonus experience for you.
    }

    //todo Logging off within X radius of a bed will also give bonus experience.
    //We'll save their names to a DB on log out if we find a bed close enough.
    //On login we check the time they logged off, calculate, remove from the DB, and give them their XP.
}
