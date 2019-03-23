package net.lordofthecraft.arche.attributes;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlot;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier.Decay;

@Accessors(fluent=true)
@FieldDefaults(level=AccessLevel.PRIVATE)
public class ModifierBuilder {
	@Setter UUID uuid = null;
	@Setter String name = "";
	@Setter double amount = 0.0;
	@Setter Operation operation = Operation.ADD_NUMBER;
	@Setter EquipmentSlot slot = null;
	
	@Setter boolean lostOnDeath = false;
	@Setter boolean shouldSave = true;
	Decay decay = Decay.NEVER;
	long ticks = 0;
	boolean modifierShowsName = true;
	boolean modifierIsVisible = true;
	
	public ModifierBuilder() {}
	
	public ModifierBuilder(AttributeModifier mod) {
		uuid = mod.getUniqueId();
		name = mod.getName();
		amount = mod.getAmount();
		operation = mod.getOperation();
	}
	
	public ModifierBuilder(String name, double amount, Operation operation) {
		this(null, name, amount, operation);
	}
	
	public ModifierBuilder(UUID uuid, String name, double amount, Operation operation) {
		Validate.notNull(name);
		Validate.notNull(operation);
		this.uuid = uuid;
		this.name = name;
		this.amount = amount;
		this.operation = operation;
	}
	
	public ModifierBuilder randomUUID() {
		uuid = UUID.randomUUID();
		return this;
	}
	
	public ModifierBuilder withDecayStrategy(Decay decay, long ticks) {
		Validate.notNull(decay);
		Validate.isTrue(ticks > 0 || decay == Decay.NEVER);
		this.decay = decay;
		this.ticks = ticks;
		return this;
	}

	public ModifierBuilder withMenuVisibility(boolean visible) {
		modifierIsVisible = visible;
		return this;
	}
	
	public ModifierBuilder invisible() {
		modifierIsVisible = false;
		return this;
	}
	
	public ModifierBuilder withNameVisibility(boolean visible) {
		modifierShowsName = visible;
		return this;
	}
	
	public ModifierBuilder hideName() {
		modifierShowsName = false;
		return this;
	}
	
	public ExtendedAttributeModifier create() {
		if(uuid == null) {
			if(name.isEmpty()) uuid = UUID.randomUUID();
			else uuid = UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
		}
		
		if(!modifierIsVisible) name = "/"+name;
		else if(!modifierShowsName && !name.isEmpty()) name = "#" + name;
		
		return new ExtendedAttributeModifier(uuid, name, amount, operation, slot, shouldSave, decay, ticks, lostOnDeath);
	}
}
