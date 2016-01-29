package net.lordofthecraft.arche.attributes;

import java.util.UUID;

import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a wrapped Minecraft Attribute Modifier that may be applied to an entity or item.
 */
public class AttributeModifier {

	private final String name;
	private final UUID id;
	private final Operation operation;
	private final AttributeType ga;
	private double value;

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
	 * Gives the human-readable name of the modifier as it will be stored by Minecraft
	 * @return the Name of the modifier
	 */
	public String getName(){return name;}
	
	public double getValue(){return value;}
	
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
	
	public void setValue(double value){
		if(operation != Operation.INCREMENT && value < -1){
			this.value = 0;
		} else{
			this.value = value;
		}
	}

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
