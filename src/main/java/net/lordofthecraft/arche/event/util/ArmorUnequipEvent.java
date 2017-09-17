package net.lordofthecraft.arche.event.util;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

import net.lordofthecraft.arche.attributes.AttributeItem.Slot;

public class ArmorUnequipEvent extends ArmorEvent {
	private static final HandlerList handlers = new HandlerList();

	public ArmorUnequipEvent(Player who, ItemStack armor, Slot slot) {
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
