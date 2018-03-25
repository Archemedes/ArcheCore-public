package net.lordofthecraft.arche.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import io.github.archemedes.customitem.CustomTag;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.attributes.items.AppliedAttributes;
import net.lordofthecraft.arche.attributes.items.AttributeHandler;
import net.lordofthecraft.arche.event.persona.PersonaActivateEvent;
import net.lordofthecraft.arche.event.persona.PersonaDeactivateEvent;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.util.ItemUtil;

public class AttributeItemListener implements Listener {
	private final AttributeHandler handle;
	
	public AttributeItemListener(){
		handle = AttributeHandler.getInstance();
		listenToPacket();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void yummy(PlayerItemConsumeEvent e) {
		Persona ps = ArcheCore.getPersona(e.getPlayer());
		if(ps != null) handle.applyConsumable(ps, e.getItem());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void click(PlayerInteractEvent e) {
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemStack item = e.getItem();
			Player p = e.getPlayer();
			Persona ps = ArcheCore.getPersona(p);
			if(ps != null) {
				
				boolean used =  handle.applyUseable(ps, item);
				if(used) {
					e.setUseItemInHand(Result.DENY);
					item.setAmount(item.getAmount() - 1);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void damage(PlayerItemDamageEvent e) {
		ItemStack item = e.getItem();
		String dura = CustomTag.getTagValue(item, "n_dura");
		
		if(dura != null) {
			int damage = e.getDamage();
			double durability = Integer.parseInt(dura);
			durability /= 100;
			durability = 1 / ( 1 + durability);

			double newDamage = damage * durability;
			int intdamage = (int) newDamage;
			double floatdamage = newDamage - intdamage;
			if(Math.random() < floatdamage) intdamage++;
			e.setDamage(intdamage);
		}
	}
	
	private void listenToPacket() {
		//Events this covers:
		//Picking up and dropping items
		//Flipping held items through F
		//Equipping armor through right-click
		ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(ArcheCore.getPlugin(), PacketType.Play.Server.SET_SLOT) {
			@Override
			public void onPacketSending(final PacketEvent event) {
				PacketContainer packet = event.getPacket();
				StructureModifier<Integer> integers = packet.getIntegers();
				int windowId = integers.read(0);
				
				if(windowId == 0) { //Means player inventory
					Player player = event.getPlayer();
					Persona ps = ArcheCore.getPersona(player);
					if(ps != null) {
						int slot = integers.read(1);
						
						EquipmentSlot target = null;
						switch(slot) {
						case 5: target = EquipmentSlot.HEAD; break;
						case 6: target = EquipmentSlot.CHEST; break;
						case 7: target = EquipmentSlot.LEGS; break;
						case 8: target = EquipmentSlot.FEET; break;
						case 45: target = EquipmentSlot.OFF_HAND; break;
						default:
							int held = player.getInventory().getHeldItemSlot() + 36;
							if(slot == held) target = EquipmentSlot.HAND;
							break;
						}
						
						if(target != null) {
							ItemStack is = packet.getItemModifier().read(0);
							newItem(ps, is, target);
						}
					}
					
				}
			}
		});
	}
	
	private void queueFullCheck(Persona ps, boolean thorough) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), ()->{
			Player p = ps.getPlayer();
			if(p == null) return;
			
			PlayerInventory pinv = p.getInventory();
			ItemStack is;

			is = pinv.getHelmet();
			if(ItemUtil.exists(is) || thorough) newItem(ps, is, EquipmentSlot.HEAD);
			is = pinv.getChestplate();
			if(ItemUtil.exists(is) || thorough) newItem(ps, is, EquipmentSlot.CHEST);
			is = pinv.getLeggings();
			if(ItemUtil.exists(is) || thorough) newItem(ps, is, EquipmentSlot.LEGS);
			is = pinv.getBoots();
			if(ItemUtil.exists(is) || thorough) newItem(ps, is, EquipmentSlot.FEET);
			is = pinv.getItemInMainHand();
			if(ItemUtil.exists(is) || thorough) newItem(ps, is, EquipmentSlot.HAND);
			is = pinv.getItemInOffHand();
			if(ItemUtil.exists(is) || thorough) newItem(ps, is, EquipmentSlot.OFF_HAND);

		});
	}
	
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void close(InventoryCloseEvent e){
		Persona ps = ArcheCore.getPersona((Player) e.getPlayer());
		if(ps == null) return;
		
		if(e.getInventory().getType() == InventoryType.CRAFTING) {
			 queueFullCheck(ps, true);
		} else {
			PlayerInventory pi = e.getPlayer().getInventory();
			int rawSlot = pi.getHeldItemSlot() + 36;
			ItemStack is = pi.getItem(rawSlot);
			newItem(ps, is, EquipmentSlot.HAND);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void hand(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		Persona ps = ArcheCore.getPersona(p);
		
		if(ps != null) {
			ItemStack item = p.getInventory().getItem(e.getNewSlot());
			newItem(ps, item, EquipmentSlot.HAND);
		}
	}
	
	@EventHandler
	public void persona(PersonaActivateEvent e) {
		Persona persona = e.getPersona();
		handle.register(persona);
		queueFullCheck(e.getPersona(), false);
	}
	
	@EventHandler
	public void persona(PersonaDeactivateEvent e) {
		handle.unregister(e.getPersona());
	}
	
	@EventHandler(ignoreCancelled = true)
	public void player(PlayerDeathEvent e) {
		Persona ps = ArcheCore.getPersona(e.getEntity());
		for(EquipmentSlot s : EquipmentSlot.values()) {
			clearItem(ps, s);
		}
	}
	
	private void clearItem(Persona p, EquipmentSlot slot) {
		newItem(p, null, slot);
	}
	
	private void newItem(Persona p, ItemStack item, EquipmentSlot slot) {
		AppliedAttributes aa = handle.getFor(p);
		aa.clearMods(slot);
		if(ItemUtil.exists(item)) handle.getAttributes(item).forEach(aa::addMod);
	}
}
