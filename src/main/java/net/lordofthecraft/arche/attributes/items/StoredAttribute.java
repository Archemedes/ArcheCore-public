package net.lordofthecraft.arche.attributes.items;

import java.util.UUID;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.ItemStack;

import io.github.archemedes.customitem.CustomTag;
import net.lordofthecraft.arche.attributes.ArcheAttribute;
import net.lordofthecraft.arche.attributes.AttributeRegistry;
import net.lordofthecraft.arche.attributes.ExtendedAttributeModifier.Decay;

public class StoredAttribute implements TagAttribute {
	private final ArcheAttribute attribute;
	private final AttributeModifier modifier;
	private final long ticks;
	private final Decay decay;
	private final boolean consume;

	public StoredAttribute(ArcheAttribute attribute, AttributeModifier modifier, long ticks, Decay decay, boolean consume) {
		this.attribute = attribute;
		this.modifier = modifier;
		this.ticks = ticks;
		this.decay = decay;
		this.consume = consume;
	}
	
	@Override
	public ArcheAttribute getAttribute() {
		return attribute;
	}
	
	@Override
	public AttributeModifier getModifier() {
		return modifier;
	}
	
	public boolean isConsumable() {
		return consume;
	}
	
	public long getTicks() {
		return ticks;
	}
	
	public Decay getDecayStrategy() {
		return decay;
	}
	
	public String asTagKey() {
		String prefix = consume? "nac" : "nar";
		return prefix + '_' + attribute.getName();
	}
	
	public String asTagValue() {
		return String.join("@", 
				modifier.getUniqueId().toString(),
				modifier.getName(),
				Double.toString(modifier.getAmount()),
				Integer.toString(modifier.getOperation().ordinal()),
				Long.toString(this.ticks),
				Integer.toString(decay.ordinal())
				);
	}
	
	public static StoredAttribute fromTag(String key, String val) {
		char c = key.charAt(2);
		boolean consume = c=='c';
		ArcheAttribute aa = AttributeRegistry.getInstance().getAttribute(key.substring(4));
		
		String[] parts = val.split("@");
		UUID uuid = UUID.fromString(parts[0]);
		String modname = parts[1];
		double amount = Double.parseDouble(parts[2]);
		Operation op = Operation.values()[Integer.parseInt(parts[3])];
		AttributeModifier mod = new AttributeModifier(uuid, modname, amount, op);

		long ticks = Long.parseLong(parts[4]);
		Decay dec = Decay.values()[Integer.parseInt(parts[5])];
		
		return new StoredAttribute(aa, mod, ticks, dec, consume);
	}
}
