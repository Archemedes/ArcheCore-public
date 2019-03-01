package net.lordofthecraft.arche.attributes.items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.util.ItemUtil;
import lombok.Getter;
import lombok.var;
import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.attributes.VanillaAttribute;

@Getter
public final class ItemAttribute implements TagAttribute {
	private final ArcheAttribute attribute;
	private final AttributeModifier modifier;
	
	public ItemAttribute(ArcheAttribute att, AttributeModifier modifier) {
		if(att instanceof VanillaAttribute) {
			throw new IllegalArgumentException("Use vanilla MC methods to apply vanilla attributes!");
		}
		
		//TODO this makes no sense right now
		Validate.notNull(modifier.getSlot());
		
		this.attribute = att;
		this.modifier = modifier;
	}


	public EquipmentSlot getSlot() {
		return modifier.getSlot();
	}
	
	@Override
	public String asTagKey() {
		return "na_" + attribute.getName();
	}
	
	@Override
	public String asTagValue() {
		return String.join("@",
				modifier.getUniqueId().toString(),
				modifier.getName(),
				Double.toString(modifier.getAmount()),
				Integer.toString(modifier.getOperation().ordinal()),
				Integer.toString(getSlot().ordinal()));
	}
	
	public static List<ItemAttribute> get(ItemStack is){
		List<ItemAttribute> result = new ArrayList<>();
		var tag = ItemUtil.getCustomTags(is);
		
		for(Entry<String, String> entry : tag.entrySet()) {
			String key = entry.getKey();
			if(key.startsWith("na_")) {
				result.add(fromTag(key, entry.getValue()));
			}
		}
		
		return result;
	}
	
	public static ItemAttribute get(ItemStack is, ArcheAttribute attribute) {
		String key = "na_" + attribute.getName();
		String value = ItemUtil.getCustomTag(is, "na_"+ attribute.getName());
		
		if(value == null) return null;
		else return fromTag(key, value);
	}
	
	public static ItemAttribute fromTag(String key, String val) {
		ArcheAttribute aa = AttributeRegistry.getInstance().getAttribute(key.substring(3));
		
		String[] parts = val.split("@");
		UUID uuid = UUID.fromString(parts[0]);
		String modname = parts[1];
		double amount = Double.parseDouble(parts[2]);
		Operation op = Operation.values()[Integer.parseInt(parts[3])];
		EquipmentSlot slot = EquipmentSlot.values()[Integer.parseInt(parts[4])];
		AttributeModifier mod = new AttributeModifier(uuid, modname, amount, op, slot);

		return new ItemAttribute(aa, mod);
	}
	
	
	
}
