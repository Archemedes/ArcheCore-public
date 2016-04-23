package net.lordofthecraft.arche.attributes;

import com.google.common.collect.Sets;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a wrapped Minecraft Attribute Modifier that may be applied to an entity or item.
 */
public class AttributeModifier {

	private final String name;
	private final UUID id;
	private final Operation operation;
	private final AttributeType ga;
	private double value;
	private Set<AttributeSlot> slots;

	/**
	 * Constructs an AttributeModifier
	 * @param uuid The UUID of the modifier
	 * @param name the human-readable name of the modifier. Has no effect besides readability.
	 * @param value The value by which to modify
	 * @param operation The type of modification this AttributeModifier should apply
	 * @param ga The Attribute to modify
	 */
	public AttributeModifier(UUID uuid,String name, double value, Operation operation, AttributeType ga){
		this.id = uuid;
		this.name = name;
		this.value = value;
		this.operation = operation;
		this.ga = ga;
		this.slots = Sets.newHashSet();
	}
	
	/**
	 * Constructs a dummy Attribute Modifier, useful for removing attributes from an entity.
	 * @param uuid The UUID of the modifier
	 * @param ga The Attribute to modify
	 */
	public AttributeModifier(UUID uuid, AttributeType ga){
		this(uuid, "dummy", 0, Operation.INCREMENT, ga);
	}

	/**
	 * Constructs an attribute modifer with slots
	 *
	 * @param uuid      The UUID of the modifier
	 * @param name      the human-readable name of the modifier. Has no effect besides readability.
	 * @param value     The value by which to modify
	 * @param operation The type of modification this AttributeModifier should apply
	 * @param ga        The Attribute to modify
	 * @param slots     The slots to apply this modifier to
	 */

	public AttributeModifier(UUID uuid, String name, double value, Operation operation, AttributeType ga, AttributeSlot[] slots) {
		this.id = uuid;
		this.name = name;
		this.value = value;
		this.operation = operation;
		this.ga = ga;
		this.slots = Sets.newHashSet();
		Collections.addAll(this.slots, slots);
	}

	/**
	 * Gives the human-readable name of the modifier as it will be stored by Minecraft
	 * @return the Name of the modifier
	 */
	public String getName(){return name;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		if (operation != Operation.INCREMENT && value < -1) {
			this.value = 0;
		} else {
			this.value = value;
		}
	}

	/**
	 * Gets the slots this attribute modifier will apply to
	 *
	 * @return The Set of slots
	 */
	public Set<AttributeSlot> getSlots() {
		return slots;
	}

	/**
	 * Set the slots of the item
	 *
	 * @param slots The set of slots
	 * @return The set of slots
	 */
	public AttributeModifier setSlots(Set<AttributeSlot> slots) {
		this.slots = slots;
		return this;
	}

	/**
	 * Returns a string which is used in the attribute modifier to define slots
	 *
	 * @return The string in the format of [value1],[value2],[value3],...
	 */
	public String getFormattedSlots() {
		StringBuilder sb = new StringBuilder();
		String b = "";
		for (AttributeSlot slot : slots) {
			sb.append(b);
			sb.append(slot);
			b = ",";
		}
		return sb.toString();
	}

	/**
	 * Add a slot which this item will apply to
	 *
	 * @param slot The slot to add
	 * @return The updated attribute modifier
	 */
	public AttributeModifier addSlot(AttributeSlot slot) {
		slots.add(slot);
		return this;
	}

	/**
	 * Retrieve the type of operation this modifier will invoke
	 * @return The type of operation for this modifier
	 */
	public int getOperation(){return operation.getValue();}
	
	/**
	 * Returns the unique UUID of the modifier. Modifiers with identical parent Attributes and UUID override each other
	 * @return the UUID of the modifier
	 */
	public UUID getUUID() {return id;}

	public AttributeType getAttribute() {
		return ga;
	}
	
	public void apply(LivingEntity e){
		AttributeBase.addModifier(e, this);
	}
	
	public ItemStack apply(ItemStack is){
		return AttributeItem.addModifier(this, is);
	}
	
	public void remove(LivingEntity e){
		AttributeBase.removeModifier(e, this);
	}
	
	public void remove(ItemStack is){
		AttributeItem.removeModifier(this, is);
	}
		
}
