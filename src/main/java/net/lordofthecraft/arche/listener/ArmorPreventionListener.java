package net.lordofthecraft.arche.listener;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.persona.PersonaSwitchEvent;
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
	public void onPersonaSwitch(PersonaSwitchEvent e) {
        if (e.getPersona().getRace() == Race.SPECTRE || e.getPersona().getRace() == Race.CONSTRUCT) {
            boolean flag = false;
            for (final ItemStack is : e.getPlayer().getInventory().getArmorContents())
				if (is != null)
					if (is.getType() != Material.AIR && is.getType() != Material.ELYTRA) {
						flag = true; break;
					}
			
			if (flag) {
				e.getPlayer().sendMessage(ChatColor.RED+"Error; Please remove your armor before switching to this persona.");
				e.setCancelled(true);
			}
			return; 
		}
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
