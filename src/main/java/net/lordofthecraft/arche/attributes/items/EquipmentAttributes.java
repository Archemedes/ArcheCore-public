package net.lordofthecraft.arche.attributes.items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import co.lotc.core.bukkit.util.ItemUtil;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.attributes.ModifierBuilder;
import net.lordofthecraft.arche.interfaces.Persona;

public class EquipmentAttributes {
	private final Persona persona;
	@SuppressWarnings("unchecked")
	private final List<ItemAttribute>[] atts = new ArrayList[EquipmentSlot.values().length];
	
	
	public EquipmentAttributes(Persona persona) {
		this.persona = persona;
		
		EquipmentSlot[] slots = EquipmentSlot.values();
		for(int i = 0; i < slots.length; i++) {
			atts[i] = new ArrayList<>();
		}
	}
	
	public Persona getPersona() {
		return persona;
	}
	
	public void queueFullCheck(boolean thorough) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), ()->{
			Player p = persona.getPlayer();
			if(p == null) return;
			
			PlayerInventory pinv = p.getInventory();
			ItemStack is;

			is = pinv.getHelmet();
			if(ItemUtil.exists(is) || thorough) newItem(is, EquipmentSlot.HEAD);
			is = pinv.getChestplate();
			if(ItemUtil.exists(is) || thorough) newItem(is, EquipmentSlot.CHEST);
			is = pinv.getLeggings();
			if(ItemUtil.exists(is) || thorough) newItem(is, EquipmentSlot.LEGS);
			is = pinv.getBoots();
			if(ItemUtil.exists(is) || thorough) newItem(is, EquipmentSlot.FEET);
			is = pinv.getItemInMainHand();
			if(ItemUtil.exists(is) || thorough) newItem(is, EquipmentSlot.HAND);
			is = pinv.getItemInOffHand();
			if(ItemUtil.exists(is) || thorough) newItem(is, EquipmentSlot.OFF_HAND);

		});
	}
	
	public void clearItem(EquipmentSlot slot) {
		newItem(null, slot);
	}
	
	public void newItem(ItemStack item, EquipmentSlot slot) {
		EquipmentAttributes aa = getPersona().attributes().getItemAttributes();
		aa.clearMods(slot);
		if(ItemUtil.exists(item)) ItemAttribute.get(item).forEach(m->aa.addMod(m, slot));
	}
	
	public void clearAllMods() {
		Arrays.stream(EquipmentSlot.values()).forEach(this::clearMods);
	}

	public void clearMods(EquipmentSlot slot) {
		List<ItemAttribute> mods = atts[slot.ordinal()];
		
		mods.forEach(m-> persona.attributes().removeModifier(m.getAttribute(), m.getModifier()));
		mods.clear();
	}
	
	private void addMod(ItemAttribute attribute, EquipmentSlot slot){
		if(slot != attribute.getSlot()) return;
		
		ExtendedAttributeModifier eam = new ModifierBuilder(attribute.getModifier())
				.shouldSave(false)
				.slot(slot)
				.create();
		
		for(List<ItemAttribute> mods : atts) {
			Iterator<ItemAttribute> ias = mods.iterator();
			while(ias.hasNext()) {
				ItemAttribute ia = ias.next();
				if(attribute.getAttribute() == ia.getAttribute() &&
						ia.getModifier().getUniqueId().equals(attribute.getModifier().getUniqueId())){
					persona.attributes().getInstance(ia.getAttribute()).removeModifier(ia.getModifier());
					ias.remove();
				}
			}
		}
		
		List<ItemAttribute> mods = atts[attribute.getSlot().ordinal()];
		mods.add(attribute);
		persona.attributes().addModifier(attribute.getAttribute(), eam);
	}
	
}
