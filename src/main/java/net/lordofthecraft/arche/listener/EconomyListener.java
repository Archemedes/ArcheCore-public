package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Economy;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.Transaction;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.Plugin;

public class EconomyListener implements Listener {
    private final Economy economy;

    public EconomyListener(Economy economy) {
        this.economy = economy;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Persona p = ArcheCore.getControls().getPersonaHandler().getPersona(e.getEntity());
        if (p != null) {
            double penalty = economy.getBalance(p) * economy.getFractionLostOnDeath();
            economy.withdrawPersona(p, penalty, new DeathTaxTransaction(p));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Villager) {
            e.setCancelled(true);
            e.getRightClicked().remove();
        }
    }

    public static class DeathTaxTransaction implements Transaction {
        private final Persona persona;

        protected DeathTaxTransaction(Persona persona) {
            this.persona = persona;
        }

        @Override
        public String getCause() {
            return persona.getPlayerName() + "@" + persona.getId() + " DIED. HAHA.";
        }

        @Override
        public TransactionType getType() {
            return TransactionType.WITHDRAW;
        }

        @Override
        public Plugin getRegisteringPlugin() {
            return ArcheCore.getPlugin();
        }

        @Override
        public String getRegisteringPluginName() {
            return "ArcheCore-Economy";
        }
    }
}
