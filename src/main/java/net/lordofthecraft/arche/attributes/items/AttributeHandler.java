package net.lordofthecraft.arche.attributes.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import io.github.archemedes.customitem.CustomTag;
import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.attributes.ModifierBuilder;
import net.lordofthecraft.arche.interfaces.Persona;

public class AttributeHandler {
	private static final AttributeHandler INSTANCE = new AttributeHandler();
	
	public static AttributeHandler getInstance() {
		return INSTANCE;
	}
	
	private final Map<Persona, AppliedAttributes> beingApplied = new HashMap<>(200);
	
	
	public AppliedAttributes getFor(Persona persona) {
		return beingApplied.get(persona);
	}
	
	public void register(Persona persona) {
		beingApplied.put(persona, new AppliedAttributes(persona));
	}
	
	public void unregister(Persona persona) {
		AppliedAttributes remove = beingApplied.remove(persona);
		for(EquipmentSlot s : EquipmentSlot.values()) {
			remove.clearMods(s);
		}
	}
	
	public ItemStack addAttribute(ItemStack is, ItemAttribute attribute) {
		CustomTag tag = CustomTag.getFrom(is);
		tag.put(attribute.asTagKey(), attribute.asTagValue());
		is = tag.apply(is);
		return is;
	}
	
	public ItemStack addAttribute(ItemStack is, StoredAttribute attribute) {
		CustomTag tag = CustomTag.getFrom(is);
		tag.put(attribute.asTagKey(), attribute.asTagValue());
		is = tag.apply(is);
		return is;
	}
	
	public ItemAttribute getAttribute(ItemStack is, ArcheAttribute attribute) {
		String key = "na_" + attribute.getName();
		String value = CustomTag.getTagValue(is, "na_" + attribute.getName());
		
		if(value == null) return null;
		else return ItemAttribute.fromTag(key, value);
	}
	
	public List<ItemAttribute> getAttributes(ItemStack is){
		List<ItemAttribute> result = new ArrayList<>();
		CustomTag tag = CustomTag.getFrom(is);
		
		for(Entry<String, String> entry : tag.entrySet()) {
			String key = entry.getKey();
			if(key.startsWith("na_")) {
				result.add(ItemAttribute.fromTag(key, entry.getValue()));
			}
		}
		
		return result;
	}
	
	public boolean applyConsumable(Persona ps, ItemStack item) {
		return apply(ps, item, "nac_");
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
