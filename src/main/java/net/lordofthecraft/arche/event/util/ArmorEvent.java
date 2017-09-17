package net.lordofthecraft.arche.event.util;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

import net.lordofthecraft.arche.attributes.AttributeItem.Slot;

public abstract class ArmorEvent extends PlayerEvent implements Cancellable{
	private ItemStack armor;
	private final Slot slot; 
	
	ArmorEvent(Player who, ItemStack armor, Slot slot) {
		super(who);
		this.armor = armor;
		this.slot = slot;
	}
	
	public Slot getSlot() {
		return slot;
	}
	
	public ItemStack getArmor() {
		return armor;
	}

	//Double boilerplate implementation
	private static final HandlerList handlers = new HandlerList();
	private boolean cancelled = false;
	
	@Override public boolean isCancelled() { return cancelled ; }
	@Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled ; }
	public static HandlerList getHandlerList() {return handlers;}
	public HandlerList getHandlers() {return handlers;}
}
