package net.lordofthecraft.arche.listener;

import java.util.Map.Entry;

import org.bukkit.attribute.AttributeModifier;
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
import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.attributes.ModifierBuilder;
import net.lordofthecraft.arche.attributes.items.StoredAttribute;
import net.lordofthecraft.arche.interfaces.Persona;
import net.md_5.bungee.api.ChatColor;

public class AttributeItemListener implements Listener {
	
	public AttributeItemListener(){
		listenToPacket();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void yummy(PlayerItemConsumeEvent e) {
		Persona ps = ArcheCore.getPersona(e.getPlayer());
		if(ps != null) applyConsumable(ps, e.getItem());
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void click(PlayerInteractEvent e) {
		if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			ItemStack item = e.getItem();
			Player p = e.getPlayer();
			Persona ps = ArcheCore.getPersona(p);
			if(ps != null) {
				if(conflictUseable(ps, item)) {
					p.sendMessage(ChatColor.RED + "You won't benefit any further from using this");
					e.setUseItemInHand(Result.DENY);
				} else if(applyUseable(ps, item)) {
					e.setUseItemInHand(Result.DENY);
					item.setAmount(item.getAmount() - 1);
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void damage(PlayerItemDamageEvent e) {
		ItemStack item = e.getItem();
		String dura = CustomTag.getTagValue(item, "durability");
		
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
							ps.attributes().getItemAttributes().newItem(is, target);
						}
					}
					
				}
			}
		});
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void close(InventoryCloseEvent e){
		Persona ps = ArcheCore.getPersona((Player) e.getPlayer());
		if(ps == null) return;
		
		if(e.getInventory().getType() == InventoryType.CRAFTING) {
			 ps.attributes().getItemAttributes().queueFullCheck(true);
		} else {
			PlayerInventory pi = e.getPlayer().getInventory();
			int rawSlot = pi.getHeldItemSlot() + 36;
			ItemStack is = pi.getItem(rawSlot);
			ps.attributes().getItemAttributes().newItem(is, EquipmentSlot.HAND);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void hand(PlayerItemHeldEvent e) {
		Player p = e.getPlayer();
		Persona ps = ArcheCore.getPersona(p);
		
		if(ps != null) {
			ItemStack item = p.getInventory().getItem(e.getNewSlot());
			ps.attributes().getItemAttributes().newItem(item, EquipmentSlot.HAND);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void player(PlayerDeathEvent e) {
		Persona ps = ArcheCore.getPersona(e.getEntity());
		for(EquipmentSlot s : EquipmentSlot.values()) {
			ps.attributes().getItemAttributes().clearItem(s);
		}
	}
	
	//
	//AttributeHandler methods: Consumeable and usable.
	//
	
	public boolean applyConsumable(Persona ps, ItemStack item) {
		return apply(ps, item, "nac_");
	}
	
	public boolean conflictUseable(Persona ps, ItemStack item) {
		boolean maybe = false;
		CustomTag tag = CustomTag.getFrom(item);
		
		for(Entry<String, String> entry : tag.entrySet()) {
			String key = entry.getKey();
			if(key.startsWith("nar_")) {
				StoredAttribute sad = StoredAttribute.fromTag(entry.getKey(), entry.getValue());
				ArcheAttribute a = sad.getAttribute();
				AttributeModifier m = sad.getModifier();
				if(ps.attributes().getInstance(a).hasModifier(m)) maybe = true;
				else return false;
			}
		}
		
		return maybe;
	}
	
	public boolean applyUseable(Persona ps, ItemStack item) {
		//if(ItemExpiry.hasExpired(item)) return false;
		return apply(ps, item, "nar_");
	}
	
	private boolean apply(Persona ps, ItemStack is, String prefix) {
		boolean result = false;
		CustomTag tag = CustomTag.getFrom(is);
		for(Entry<String, String> entry : tag.entrySet()) {
			String key = entry.getKey();
			if(key.startsWith(prefix)) {
				result = true;
				StoredAttribute sad = StoredAttribute.fromTag(entry.getKey(), entry.getValue());
				apply(ps, sad);
			}
		}
		
		return result;
	}
		
	private void apply(Persona ps, StoredAttribute att) {
		ExtendedAttributeModifier eam = new ModifierBuilder(att.getModifier())
				.withDecayStrategy(att.getDecayStrategy(), att.getTicks())
				.create();
		
		ps.attributes().addModifier(att.getAttribute(), eam);
	}
}
