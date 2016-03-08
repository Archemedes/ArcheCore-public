package net.lordofthecraft.arche.listener;

import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.event.PersonaSwitchEvent;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.interfaces.PersonaHandler;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.entity.EntityMountEvent;

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
				if (is.getType() != Material.AIR) { flag = true; break; }
			
			if (flag) {
				e.getPlayer().sendMessage(ChatColor.RED+"Error; Please remove your armor before switching to this persona.");
				e.setCancelled(true);
			}
			return;
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent e){
		Player p = (Player) e.getWhoClicked();
		switch (e.getAction()) {
		case PLACE_ALL: case PLACE_SOME: case PLACE_ONE: case SWAP_WITH_CURSOR:
			ItemStack armor = e.getCursor();
			if (armor != null){
				if (!isArmor(armor.getType()))
					return;
				if(!canEquip(p, armor) && e.getSlotType() == InventoryType.SlotType.ARMOR){
					e.setCancelled(true);
					return;
				}
			}
			break;
		case HOTBAR_SWAP:
			int b = e.getHotbarButton();
			ItemStack armor3 = p.getInventory().getItem(b);
			if (armor3 != null){
				if (!isArmor(armor3.getType()))
					return;
				if(!canEquip(p, armor3) && e.getSlotType() == InventoryType.SlotType.ARMOR){
					e.setCancelled(true);
					return;
				}
			}
			break;
		case MOVE_TO_OTHER_INVENTORY:
			ItemStack armor2 = e.getCurrentItem();
			if (armor2 != null){
				if (!isArmor(armor2.getType()))
					return;
				if(!canEquip(p, armor2) && e.getSlotType() != InventoryType.SlotType.ARMOR){
					e.setCancelled(true);
					return;
				}
			}
			break;
		default:
			break;
		}
		return;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerInteract(PlayerInteractEvent e){
		Player p = e.getPlayer();
		ItemStack armor = p.getItemInHand();
		if ((e.getAction()==Action.RIGHT_CLICK_AIR || e.getAction()==Action.RIGHT_CLICK_BLOCK) 
				&& isArmor(armor.getType()) && !canEquip(p, armor)){
			e.setCancelled(true);
			p.updateInventory();
			return;
		}
	}

	/*	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e){
		ItemStack armor = e.getOldCursor();
		Player p = (Player) e.getWhoClicked();
		String inv =  e.getInventorySlots().toString();
		if (canEquip(p , armor) == true){
			boolean can = true;
			for (int i=36 ; i<=39 ; i++){
				String s = new Integer(i).toString();
				if (inv.contains(s)){
					can = false;
				}
			}
			if (can==false){
				e.setCancelled(true);
			}
		}
	}*/

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e){
		Player p = (Player) e.getPlayer();
		Inventory inv = p.getInventory();
		for (int i=36 ; i<=39 ;i++ ){
			if (!(inv.getItem(i)==null)){
				if (!canEquip(p, inv.getItem(i))){
					ItemStack item = inv.getItem(i);
					inv.setItem(i, null);
					p.getWorld().dropItemNaturally(p.getLocation(), new ItemStack(item));
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityMount(EntityMountEvent e) {
		if (e.getEntity() instanceof Player && e.getMount() instanceof Horse) {
			final Player p = (Player) e.getEntity();
			if (isWearingIron(p.getInventory().getArmorContents())) {
				e.setCancelled(true);
				p.sendMessage(ChatColor.RED+"You struggle and eventually fail to mount your steed in your heavy, clunky armor");
			}
		}
	}

	private boolean canEquip(Player p, ItemStack armor) {
		Persona ps = handler.getPersona(p);
		if (p.isInsideVehicle()) {
			if (p.getVehicle() instanceof Horse && armor != null) {
				if (armor.getType()==Material.IRON_HELMET
						|| armor.getType()==Material.IRON_CHESTPLATE
						|| armor.getType()==Material.IRON_LEGGINGS
						|| armor.getType()==Material.IRON_BOOTS) {
					return false;
				}
			}
		}
		return !(ps != null && (ps.getRace() == Race.CONSTRUCT || ps.getRace() == Race.SPECTRE));
	}

	
	private boolean isWearingIron(ItemStack[] armor) {
		for (int i = 0; i < armor.length; i++) {
			if (armor[i] != null) {
				if (armor[i].getType() == Material.IRON_HELMET 
						|| armor[i].getType() == Material.IRON_CHESTPLATE
						|| armor[i].getType() == Material.IRON_LEGGINGS
						|| armor[i].getType() == Material.IRON_BOOTS) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean isArmor(Material m){
		switch(m){
		case JACK_O_LANTERN: case GOLD_HELMET: case GOLD_BOOTS: case GOLD_CHESTPLATE: case GOLD_LEGGINGS: 
		case LEATHER_HELMET: case LEATHER_CHESTPLATE: case LEATHER_BOOTS: case LEATHER_LEGGINGS:
		case CHAINMAIL_HELMET: case CHAINMAIL_BOOTS: case CHAINMAIL_CHESTPLATE: case CHAINMAIL_LEGGINGS:
		case IRON_HELMET: case IRON_BOOTS: case IRON_CHESTPLATE: case IRON_LEGGINGS:
		case DIAMOND_HELMET: case DIAMOND_BOOTS: case DIAMOND_CHESTPLATE: case DIAMOND_LEGGINGS:
			return true;
		default: return false;
		}
	}


}