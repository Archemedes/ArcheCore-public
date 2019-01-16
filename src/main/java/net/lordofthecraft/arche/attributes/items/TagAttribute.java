package net.lordofthecraft.arche.attributes.items;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;

import co.lotc.core.bukkit.util.ItemUtil;
import net.lordofthecraft.arche.attributes.ArcheAttribute;

public interface TagAttribute {
	
	default ItemStack apply(ItemStack is) {
		ItemStack clone = new ItemStack(is);
		ItemUtil.setCustomTag(clone, asTagKey(), asTagValue());
		return clone;
	}
	
	ArcheAttribute getAttribute();
	
	AttributeModifier getModifier();
	
	String asTagKey();
	
	String asTagValue();
}
