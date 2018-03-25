package net.lordofthecraft.arche.attributes.items;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.inventory.EquipmentSlot;

import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier;
import net.lordofthecraft.arche.attributes.ModifierBuilder;
import net.lordofthecraft.arche.interfaces.Persona;

public class AppliedAttributes {
	private final Persona persona;
	@SuppressWarnings("unchecked")
	private final List<ItemAttribute>[] atts = new ArrayList[EquipmentSlot.values().length]; 
	
	
	public AppliedAttributes(Persona persona) {
		this.persona = persona;
		
		EquipmentSlot[] slots = EquipmentSlot.values();
		for(int i = 0; i < slots.length; i++) {
			atts[i] = new ArrayList<>();
		}
	}
	
	public Persona getPersona() {
		return persona;
	}

	public void clearMods(EquipmentSlot slot) {
		List<ItemAttribute> mods = atts[slot.ordinal()];
		
		mods.forEach(m-> persona.attributes().removeModifier(m.getAttribute(), m.getModifier()));
		mods.clear();
	}
	
	public void addMod(ItemAttribute attribute){
		ExtendedAttributeModifier eam = new ModifierBuilder(attribute.getModifier())
				.shouldSave(false)
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
