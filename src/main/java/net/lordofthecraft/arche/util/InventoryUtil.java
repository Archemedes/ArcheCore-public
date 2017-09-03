package net.lordofthecraft.arche.util;

import static org.bukkit.event.inventory.InventoryAction.*;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.ArcheTimer;
import net.minecraft.server.v1_12_R1.Container;
import net.minecraft.server.v1_12_R1.Slot;

public class InventoryUtil {

	public static int countInventory(Inventory inv, ItemStack is) {
		int count = 0;
		for(ItemStack other : inv.getContents()) {
			if(other == null) continue;
			if(other.isSimilar(is)) count += other.getAmount();
		}
		return count;
	}
	
	public static boolean isEmpty(Inventory inv) {
		for(ItemStack is : inv.getContents()) {
			if(is != null) return false;
		}
		return true;
	}
	
	public static boolean mightItemGetMoved(InventoryClickEvent e, ItemStack is) {
		return true;
	}
	
	/**
	 * Represents an item that was moved during a certain InventoryInteractEvent
	 * Magic values CURSOR_SLOT and DROPPED_SLOT represent items moved from 
	 * and to cursor, or items dropped to the ground
	 * slots are always the raw slots
	 */
	public static class MovedItem{
		public static final int CURSOR_SLOT = -1;
		public static final int DROPPED_SLOT = -2;
		private final ItemStack is;
		private int initialSlot;
		private final int finalSlot;
		
		public MovedItem(ItemStack is, int initial, int fin) {
			this.is = is;
			initialSlot = initial;
			finalSlot = fin;
		}
		
		public ItemStack getItem() { return is; }
		public int getInitialSlot() { return initialSlot; }
		public int getFinalSlot() { return finalSlot; }
	}
	
	public static List<MovedItem> getResultOfEvent(InventoryInteractEvent e){
		ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
		String dbg = null;

		List<MovedItem> result = new ArrayList<>();
		
		try {		
			if(timer != null) {
				dbg = "getResultOfEvent " + (e instanceof InventoryClickEvent? 
						((InventoryClickEvent) e).getAction() : ((InventoryDragEvent) e).getType());
				timer.startTiming(dbg);
			}

			if(e.isCancelled()) return result;

			if(e instanceof InventoryClickEvent) {
				InventoryClickEvent ev = (InventoryClickEvent) e;
				if(ev.getClick() == ClickType.CREATIVE) return result;


				ItemStack is;
				int amount, raw;
				InventoryAction a = ev.getAction();
				switch(a) {
				case CLONE_STACK:
					//Cloning of stacks OUTSIDE of creative inventory (so player in creative mode but e.g. in chest)
					//Since this doesnt move any items (just makes them out of thin air), do nothing
					break;
				case COLLECT_TO_CURSOR: 
					is = ev.getCursor();
					InventoryView v = ev.getView();
					InventoryType type = v.getType();
					int upper = v.countSlots() - (v.getType() == InventoryType.CRAFTING? 5 : 0);
					int count = is.getAmount();
					List<Integer> collected = Lists.newArrayList();
					//Goes in 2 phases: First collect from non-maxed stacks, then also maxed stacks
					for(int phase = 0; phase < 2; phase++) {
						for(int j = 0; j < upper; j++) {
							if((type == InventoryType.CRAFTING || type == InventoryType.WORKBENCH) && j == 0)
								continue; //Can't collect from the crafting result slot
							ItemStack is2 = v.getItem(j);
							if(!collected.contains(j) && is.isSimilar(is2) && is2.getAmount() <= is.getMaxStackSize() 
									&& (phase == 1 || is2.getAmount() != is2.getMaxStackSize())) {
								is2.getAmount();
								int toAdd = Math.min(is.getMaxStackSize() - is.getAmount(), is2.getAmount());
								is2 = is2.clone();
								is2.setAmount(toAdd);
								result.add(new MovedItem(is2, j, MovedItem.CURSOR_SLOT));
								count += toAdd;
								if(count >= is.getMaxStackSize()) return result;
								collected.add(j);
							}
						}
					}
					break;
				case DROP_ALL_CURSOR: case DROP_ONE_CURSOR:
					is = ev.getCursor().clone();
					if(a == DROP_ONE_CURSOR) {
						is.setAmount(1);
					}
					result.add(new MovedItem(is, MovedItem.CURSOR_SLOT, MovedItem.DROPPED_SLOT));
					break;
				case DROP_ALL_SLOT: case DROP_ONE_SLOT:
					is = ev.getCurrentItem();
					if(a == DROP_ONE_SLOT) is.setAmount(1);
					result.add(new MovedItem(is, ev.getRawSlot(), MovedItem.DROPPED_SLOT));
					break;
				case HOTBAR_MOVE_AND_READD: //This is chosen instead of HOTBAR_SWAP if:
					//there's an item in the hovered-over slot &&
					//the targeted hotbar slot is not empty &&
					//the hovered-over slot is in the top inventory || slot doesn't allow the targeted hotbar item

					//Items are moved only if an exchange of 2 ItemStacks is done between top inventory to hotbar
					//And slot.isAllowed(hotbarStack) == true
				case HOTBAR_SWAP:
					int hotbar = ev.getHotbarButton();
					int hotbarRawSlot = e.getView().countSlots() - 14 + hotbar;
					if(ev.getView().getType() == InventoryType.CRAFTING) hotbarRawSlot += 4;
					raw = ev.getRawSlot();
					is = ev.getView().getItem(hotbarRawSlot);
					if(raw != hotbarRawSlot && isItemAllowed(raw, is, ev.getView())) {
						
						//Enchanting table item slot only accepts count 1
						if(is.getAmount() > 1 && isEnchantingSlot(raw, ev.getView())) {
							if(a == HOTBAR_SWAP) {
								is = is.clone();
								is.setAmount(1);
							} else { //HOTBAR_MOVE_AND_READD.
								return result; //Both slots occupied but hotbar is full. Event can't move items.
							}
						}
						
						if(is.getType() != Material.AIR) result.add(new MovedItem(is.clone(), hotbarRawSlot, raw));
						is = ev.getCurrentItem();
						if(is.getType() != Material.AIR) result.add(new MovedItem(is.clone(), raw, hotbarRawSlot));
					}
					break;
				case MOVE_TO_OTHER_INVENTORY: 
					//Just cancel this event tbh
					handleMoveToOther(result, ev.getRawSlot(), ev.getView());
					break;
				case PICKUP_ALL: case PICKUP_HALF:case PICKUP_ONE:
					is = ev.getCurrentItem().clone();
					amount = a == PICKUP_ALL? is.getAmount() :
						a == PICKUP_HALF? is.getAmount()/2 + is.getAmount()%2 : 1;
						is.setAmount(amount);
						result.add(new MovedItem(is, ev.getRawSlot(), MovedItem.CURSOR_SLOT));
						break;
				case PICKUP_SOME: //When an itemstack is oversized (rare case)
					is = ev.getCurrentItem().clone();
					int stackSize = Math.min(is.getMaxStackSize(), ev.getClickedInventory().getMaxStackSize());
					int initial = ev.getCurrentItem().getAmount(); //For this InventoryAction: initial > stackSize
					amount = -1 * (stackSize - initial);
					result.add(new MovedItem(is, ev.getRawSlot(), MovedItem.CURSOR_SLOT));
				case PLACE_ALL: case PLACE_ONE: case PLACE_SOME:
					is = ev.getCursor().clone(); //Item in cursor is gonna be placed;
					raw = ev.getRawSlot();
					if(isItemAllowed(raw, is, ev.getView())) {
						amount = isEnchantingSlot(raw, ev.getView())? 1 :
							a == PLACE_ALL? is.getAmount() :
							a == PLACE_ONE? 1 : //else it's place some
								Math.min(is.getMaxStackSize(), ev.getClickedInventory().getMaxStackSize()) - ev.getCurrentItem().getAmount();
						is.setAmount(amount);
						result.add(new MovedItem(is, MovedItem.CURSOR_SLOT, raw));
					}
					break;
				case SWAP_WITH_CURSOR:
					is = ev.getCursor().clone(); 
					result.add(new MovedItem(is, MovedItem.CURSOR_SLOT, ev.getRawSlot()));
					is = ev.getCurrentItem().clone();
					result.add(new MovedItem(is, ev.getRawSlot(), MovedItem.CURSOR_SLOT));
				default:
					break;

				}
			} else { //must be drag since only 2 subinterfaces in Bukkit
				InventoryDragEvent ev = (InventoryDragEvent) e;
				ev.getNewItems().forEach( (s,is) ->result.add(new MovedItem(is, MovedItem.CURSOR_SLOT, s)));
			}


			return result;
		}finally {
			if(ArcheCore.getPlugin().debugMode()) {
				result.forEach(i -> ArcheCore.getPlugin().getLogger().info("[Debug] MovedItem {" + i.getInitialSlot() +"}->{"+ i.getFinalSlot() + "}: " + i.getItem()));
			}
			if(timer != null) timer.stopTiming(dbg);
		}
	}
	
	private static boolean isEnchantingSlot(int raw, InventoryView v) {
		return raw == 0 && v.getType() == InventoryType.ENCHANTING;
	}
	
	//TODO not nms stuff
	private static boolean isItemAllowed(int rawSlot, ItemStack is, InventoryView view) {
		Container container = ((CraftInventoryView) view).getHandle();
		Slot slot = container.getSlot(rawSlot);
		return slot.isAllowed(CraftItemStack.asNMSCopy(is));
	}
	
	private static void handleMoveToOther(List<MovedItem> result, int raw, InventoryView view) {
		switch(view.getType()) {
		case ANVIL:
		case BEACON:
		case BREWING:
		case ENCHANTING:
		case CREATIVE: //Shouldn't reach here in the first place
		case FURNACE:
			throw new UnsupportedOperationException("MOVE_TO_OTHER_INVENTORY functionality unclear for InventoryType " + view.getType());
		case CHEST:
		case ENDER_CHEST:
		case DISPENSER:
		case DROPPER:
		case HOPPER:
		case SHULKER_BOX:
			final int topSize = view.getTopInventory().getSize();
			boolean topToBottom = raw < view.getTopInventory().getSize();
			ItemStack is = view.getItem(raw);
			int initialValue = topToBottom?  view.countSlots() - 6 : 0;
			int finalValue = topToBottom? topSize : topSize - 1;
			int modder = topToBottom? -1 : 1;
			
			int amount = is.getAmount(); //Amount to move to the next inventory
			int maxRoom = Math.min(is.getMaxStackSize(), 
					topToBottom? view.getBottomInventory().getMaxStackSize() : view.getTopInventory().getMaxStackSize());
			//Moving goes in 2 phases: First fill up existing stacks, then look for empty slots
			for(int phase = 0; phase < 2; phase++) {
				for(int i = initialValue; i != finalValue; i += modder) {
					ItemStack slot = view.getItem(i);
					if( (phase == 0 && slot.isSimilar(is)) || (phase == 1 && slot.getType() == Material.AIR)) {
						int room = maxRoom - slot.getAmount();
						if(room > 0) {
							int toMove = Math.min(room, amount);
							ItemStack moved = is.clone();
							moved.setAmount(toMove);
							result.add(new MovedItem(moved, raw, i));
							amount -= toMove;
							if(amount <= 0) return;
						}
					}
				}
			}
			break;
		case CRAFTING: //This is the player inventory view
			//Complex stuff here
		case MERCHANT:
		case WORKBENCH:
		//Behavior of these inventories:
		//Anything in the bottom (player) inv is moved from hotbar to invspace or invspace to hotbar
		//The crafting result slot (raw slot 0) gets moved to inventory in reverse order
			throw new UnsupportedOperationException("MOVE_TO_OTHER_INVENTORY functionality unclear for InventoryType " + view.getType());
		default:
			break;
		
		}
	}
}
