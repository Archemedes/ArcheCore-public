package net.lordofthecraft.arche.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.util.ArmorEquipEvent;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;

public class ArmorPreventionListener implements Listener {
	private final PersonaHandler handler;

	public ArmorPreventionListener(){
		handler = ArchePersonaHandler.getInstance();
	}
	
	@EventHandler(ignoreCancelled = true)
	public void armor(ArmorEquipEvent e) {
		if(!canEquip(e.getPlayer(), e.getArmor())) {
			e.setCancelled(true);
		}
	}

	private boolean canEquip(Player p, ItemStack armor) {
		Persona ps = handler.getPersona(p);
        return !(ps != null && (ps.getRace() == Race.CONSTRUCT || ps.getRace() == Race.SPECTRE) && armor.getType() != Material.ELYTRA);
    }

}
