package net.lordofthecraft.arche.attributes.items;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;

import io.github.archemedes.customitem.CustomTag;
import net.lordofthecraft.arche.attributes.ArcheAttribute;

public interface TagAttribute {
	
	default ItemStack apply(ItemStack is) {
		CustomTag tag = CustomTag.getFrom(is);
		tag.put(asTagKey(), asTagValue());
		return tag.apply(is);
	}
	
	ArcheAttribute getAttribute();
	
	AttributeModifier getModifier();
	
	String asTagKey();
	
	String asTagValue();
}
