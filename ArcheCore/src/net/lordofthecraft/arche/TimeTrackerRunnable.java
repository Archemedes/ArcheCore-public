package net.lordofthecraft.arche;

import org.bukkit.scheduler.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.util.*;
import net.lordofthecraft.arche.persona.*;

public class TimeTrackerRunnable extends BukkitRunnable
{
    private final ArchePersonaHandler psh;
    
    TimeTrackerRunnable(final ArchePersonaHandler psh) {
        super();
        this.psh = psh;
    }
    
    public void run() {
        for (final Player p : Bukkit.getServer().getOnlinePlayers()) {
            final ArchePersona pers = this.psh.getPersona(p);
            if (pers != null) {
                pers.addTimePlayed(1);
            }
        }
    }
}
