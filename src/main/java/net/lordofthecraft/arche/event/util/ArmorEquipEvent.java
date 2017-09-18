package net.lordofthecraft.arche.event.util;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ArmorEquipEvent extends ArmorEvent {
	private static final HandlerList handlers = new HandlerList();

	public ArmorEquipEvent(Player who, ItemStack armor, EquipmentSlot slot) {
		super(who, armor, slot);
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
}
