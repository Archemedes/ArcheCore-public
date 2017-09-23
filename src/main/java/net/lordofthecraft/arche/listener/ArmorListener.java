package net.lordofthecraft.arche.listener;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import net.lordofthecraft.arche.event.util.ArmorEquipEvent;
import net.lordofthecraft.arche.event.util.ArmorUnequipEvent;
import net.lordofthecraft.arche.util.InventoryUtil;
import net.lordofthecraft.arche.util.InventoryUtil.MovedItem;

public class ArmorListener implements Listener {

	
	@EventHandler(priority = EventPriority.HIGH)
	public void onClick(PlayerInteractEvent e) {
		
		if(( e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) 
				&& !e.isBlockInHand() && e.getItem() != null) {
			ItemStack item = e.getItem();
			EquipmentSlot armorSlot = isArmor(item.getType());
			if(armorSlot != null) {
				ArmorEquipEvent aee = new ArmorEquipEvent(e.getPlayer(), item, armorSlot);
				Bukkit.getPluginManager().callEvent(aee);
				if(aee.isCancelled()) {
					e.setUseItemInHand(Result.DENY);
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e){		
		if(e.getInventory().getType() == InventoryType.CRAFTING) { //Can only equip while in the player inv screen
			switch(e.getAction()) {
				case PLACE_ALL: case PLACE_SOME: case PLACE_ONE: 
				case SWAP_WITH_CURSOR:
				case HOTBAR_SWAP: case HOTBAR_MOVE_AND_READD:
				case DROP_ONE_SLOT: case DROP_ALL_SLOT:
				case COLLECT_TO_CURSOR:
					List<MovedItem> moved = InventoryUtil.getResultOfEvent(e);
					for(MovedItem m : moved) {
						
						int raw = m.getFinalSlot();
						int init = m.getInitialSlot();
						if(raw >= 5 && raw <= 8) {
							EquipmentSlot slot = asSlot(raw);
							ArmorEquipEvent aee = new ArmorEquipEvent((Player) e.getWhoClicked(), m.getItem(), slot);
							Bukkit.getPluginManager().callEvent(aee);
							if(aee.isCancelled()) {
								e.setCancelled(true);
							}
							
							return; //Can't ever have more than 1 armor equipped per event
						} else if(init >= 5 && init <= 8) {
							EquipmentSlot slot = asSlot(init);
							ArmorUnequipEvent aue = new ArmorUnequipEvent((Player) e.getWhoClicked(), m.getItem(), slot);
							Bukkit.getPluginManager().callEvent(aue);
							if(aue.isCancelled()) {
								e.setCancelled(true);
							}
							
							return; //Can't ever have more than 1 armor equipped per event
						}
					}
				default: break;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onDrag(InventoryDragEvent e) {
		if(e.getInventory().getType() == InventoryType.CRAFTING) {
			List<MovedItem> moved = InventoryUtil.getResultOfEvent(e);
			for(MovedItem m : moved) {
				int raw = m.getFinalSlot();
				if(raw >= 5 && raw <= 8) {
					EquipmentSlot slot = asSlot(raw);
					ArmorEquipEvent aee = new ArmorEquipEvent((Player) e.getWhoClicked(), m.getItem(), slot);
					Bukkit.getPluginManager().callEvent(aee);
					if(aee.isCancelled()) {
						e.setCancelled(true);
					}
					
					return; //Can't ever have more than 1 armor equipped per event
				}
			}
		}
	}
	
	public static EquipmentSlot isArmor(Material m){
		switch(m){
		case LEATHER_HELMET: case CHAINMAIL_HELMET: case IRON_HELMET: case GOLD_HELMET: case DIAMOND_HELMET:
			return EquipmentSlot.HEAD;
		case LEATHER_CHESTPLATE: case CHAINMAIL_CHESTPLATE: case IRON_CHESTPLATE:
		case GOLD_CHESTPLATE: case DIAMOND_CHESTPLATE: case ELYTRA:
			return EquipmentSlot.CHEST;
		case LEATHER_LEGGINGS: case CHAINMAIL_LEGGINGS: case IRON_LEGGINGS: case GOLD_LEGGINGS: case DIAMOND_LEGGINGS:
			return EquipmentSlot.LEGS;
		case LEATHER_BOOTS: case CHAINMAIL_BOOTS: case IRON_BOOTS: case GOLD_BOOTS: case DIAMOND_BOOTS:
			return EquipmentSlot.FEET;
		default: return null;
		}
	}
	
	private EquipmentSlot asSlot(int raw) {
		switch(raw) {
		case 5: return EquipmentSlot.HEAD;
		case 6: return EquipmentSlot.CHEST;
		case 7: return EquipmentSlot.LEGS;
		case 8: return EquipmentSlot.FEET;
		default: throw new NullPointerException("Wrong slot you shouldn't be seeing this");
		}
	}

	
}
