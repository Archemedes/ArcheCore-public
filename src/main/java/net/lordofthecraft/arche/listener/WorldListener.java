package net.lordofthecraft.arche.listener;

import java.util.Objects;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.persona.ArchePersona;

public class WorldListener implements Listener {

	@EventHandler
	public void onSave(WorldSaveEvent e) {
		e.getWorld().getPlayers().stream()
			.map(ArcheCore::getPersona)
			.filter(Objects::nonNull)
			.map(ArchePersona.class::cast)
			.forEach(ArchePersona::saveMinecraftSpecifics);
	}
	
}
