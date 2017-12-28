package net.lordofthecraft.arche.attributes;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;

import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier.Decay;

public class ModifierBuilder {
	private UUID uuid = null;
	private String name = "";
	private double amount = 0.0;
	private Operation operation = Operation.ADD_NUMBER;
	
	private boolean save = true;
	private Decay decay = Decay.NEVER;
	private long ticks = 0;
	private boolean lostOnDeath = false;
	private boolean modifierShowsName = true;
	private boolean modifierIsVisible = true;
	
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
	
	public ModifierBuilder uuid(UUID uuid) {
		this.uuid = uuid;
		return this;
	}
	
	public ModifierBuilder randomUUID() {
		uuid = UUID.randomUUID();
		return this;
	}
	
	public ModifierBuilder name(String name) {
		Validate.notNull(name);
		this.name = name;
		return this;
	}
	
	public ModifierBuilder amount(double amount) {
		this.amount = amount;
		return this;
	}
	
	public ModifierBuilder operation(Operation op) {
		Validate.notNull(operation);
		operation = op;
		return this;
	}
	
	public ModifierBuilder shouldSave(boolean save) {
		this.save = save;
		return this;
	}
	
	public ModifierBuilder withDecayStrategy(Decay decay, long ticks) {
		Validate.notNull(decay);
		Validate.isTrue(ticks > 0);
		this.decay = decay;
		this.ticks = ticks;
		return this;
	}
	
	public ModifierBuilder lostOnDeath(boolean lost) {
		lostOnDeath = lost;
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
		
		return new ExtendedAttributeModifier(uuid, name, amount, operation, save, decay, ticks, lostOnDeath);
	}
}
