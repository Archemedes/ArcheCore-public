package net.lordofthecraft.arche.attributes.items;

import java.util.UUID;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;

import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.attributes.VanillaAttribute;

public final class ItemAttribute {
	private final ArcheAttribute attribute;
	private final AttributeModifier modifier;
	private final EquipmentSlot slot;
	
	public ItemAttribute(ArcheAttribute att, AttributeModifier modifier, EquipmentSlot slot) {
		if(att instanceof VanillaAttribute) {
			throw new IllegalArgumentException("Use vanilla MC methods to apply vanilla attributes!");
		}
		
		this.attribute = att;
		this.modifier = modifier;
		this.slot = slot;
	}
	
	public ArcheAttribute getAttribute() {
		return attribute;
	}
	
	public AttributeModifier getModifier() {
		return modifier;
	}
	
	public EquipmentSlot getSlot() {
		return slot;
	}
	
	public String asTagKey() {
		return "na_" + attribute.getName();
	}
	
	public String asTagValue() {
		return String.join("@", 
				modifier.getUniqueId().toString(),
				modifier.getName(),
				Double.toString(modifier.getAmount()),
				Integer.toString(modifier.getOperation().ordinal()),
				Integer.toString(slot.ordinal()));
	}
	
	public static ItemAttribute fromTag(String key, String val) {
		ArcheAttribute aa = AttributeRegistry.getInstance().getAttribute(key.substring(3));
		
		String[] parts = val.split("@");
		UUID uuid = UUID.fromString(parts[0]);
		String modname = parts[1];
		double amount = Double.parseDouble(parts[2]);
		Operation op = Operation.values()[Integer.parseInt(parts[3])];
		AttributeModifier mod = new AttributeModifier(uuid, modname, amount, op);

		EquipmentSlot slot = EquipmentSlot.values()[Integer.parseInt(parts[4])];
		
		return new ItemAttribute(aa, mod, slot);
	}
}
