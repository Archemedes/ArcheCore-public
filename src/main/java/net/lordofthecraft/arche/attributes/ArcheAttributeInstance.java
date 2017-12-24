package net.lordofthecraft.arche.attributes;

import com.google.common.base.Preconditions;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.PersonaKey;
import net.lordofthecraft.arche.save.rows.attribute.AttributeInsertRow;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;

import java.util.*;

public class ArcheAttributeInstance implements AttributeInstance {
	private final ArcheAttribute parent;
	private final PersonaKey persona;
	
	private Map<UUID, ExtendedAttributeModifier> mods = new HashMap<>(8);
	private double baseValue;
	
	public ArcheAttributeInstance(ArcheAttribute na, PersonaKey k) {
		this(na, k, na.getDefaultValue());
	}
	
	public ArcheAttributeInstance(ArcheAttribute na, PersonaKey k, double baseValue) {
		this.parent = na;
		this.persona = k;
		this.baseValue = baseValue;
	}
		
	
	@Override
	public Attribute getAttribute() {
		throw new UnsupportedOperationException("Custom Attribute Instance doesn't exist in Bukkit Attribute enum");
	}
	
	public ArcheAttribute getArcheAttribute() {
		return parent;
	}
	
	public PersonaKey getPersona() {
		return persona;
	}

	@Override
	public double getBaseValue() {
		return baseValue;
	}

	@Override
	public void setBaseValue(double value) {
		//This can be changed for whatever reason but it wont be persistent
		baseValue = value;
	}

	@Override
	public Collection<AttributeModifier> getModifiers() {
		//We copy the list here instead to emulate Bukkit behavior as close as possible
		//Even though Collections.unmodifiableList would be faster
		
		return new ArrayList<>(mods.values());
	}

	@Override
	public void addModifier(AttributeModifier modifier) {
		//I would prefer if the existing modifier would be replaced by the new modifier
		//But this is how this function behaves in vanilla in case of conflict
        addModifier(modifier, false);
    }
	
    public boolean addModifier(AttributeModifier modifier, boolean force) {
        Preconditions.checkArgument(modifier != null, "modifier");
        UUID uuid = modifier.getUniqueId();
        boolean exists = mods.containsKey(uuid);
        if (exists && !force) {
            throw new IllegalArgumentException("Modifier is already applied on this CUSTOM(ARCHE) attribute!");
        } else {

            ExtendedAttributeModifier mm = modifier instanceof ExtendedAttributeModifier ?
                    (ExtendedAttributeModifier) modifier : new ExtendedAttributeModifier(modifier);
            mods.put(uuid, mm);
            if (mm.save) ArcheCore.getConsumerControls().queueRow(new AttributeInsertRow(mm, persona.getPersona(), parent));
            return !exists;
        }
    }
    
    public void fromSQL(ExtendedAttributeModifier modifier) {
    	mods.put(modifier.getUniqueId(), modifier);
    }

	@Override
	public void removeModifier(AttributeModifier modifier) {
        Preconditions.checkArgument(modifier != null, "modifier");
        ExtendedAttributeModifier remove = mods.remove(modifier.getUniqueId());
        remove.remove(persona.getPersona(), parent);
	}
	
	public boolean hasModifier(AttributeModifier modifier) {
		return mods.containsKey(modifier.getUniqueId());
	}

	@Override
	public double getValue() {
		//Minecraft-like computation of base with all attributes
		double result = baseValue;
		//Three phases, one for each operation.
		for(AttributeModifier m : mods.values()) {
			if(m.getOperation() == Operation.ADD_NUMBER)
				result += m.getAmount();
		}
		double multiplier = 1.0;
		for(AttributeModifier m : mods.values()) {
			if(m.getOperation() == Operation.ADD_SCALAR)
				multiplier += m.getAmount();
		}
		
		result *= multiplier;
		for(AttributeModifier m : mods.values()) {
			if(m.getOperation() == Operation.MULTIPLY_SCALAR_1)
				result *= (1.0 + m.getAmount());
		}
		return result;
	}

	@Override
	public double getDefaultValue() {
		return parent.getDefaultValue();
	}
	

	@Override
	public int hashCode() {
		return parent.getName().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(this.getClass() != o.getClass()) return false;
		
		ArcheAttributeInstance other = (ArcheAttributeInstance) o;
		return this.getArcheAttribute() == other.getArcheAttribute()
				&& this.persona == other.persona;
	}

}
