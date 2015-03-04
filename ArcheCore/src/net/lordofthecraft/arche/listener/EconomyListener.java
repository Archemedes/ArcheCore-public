package net.lordofthecraft.arche.listener;

import org.bukkit.event.entity.*;
import net.lordofthecraft.arche.*;
import net.lordofthecraft.arche.interfaces.*;
import org.bukkit.event.*;

public class EconomyListener implements Listener
{
    private final Economy economy;
    
    public EconomyListener(final Economy economy) {
        super();
        this.economy = economy;
    }
    
    @EventHandler
    public void onDeath(final PlayerDeathEvent e) {
        final Persona p = ArcheCore.getControls().getPersonaHandler().getPersona(e.getEntity());
        if (p != null) {
            final double penalty = this.economy.getBalance(p) * this.economy.getFractionLostOnDeath();
            this.economy.withdrawPersona(p, penalty);
        }
    }
}
