package net.lordofthecraft.arche.attributes;

import java.util.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;

public class AttributeModifier
{
    private final String name;
    private final UUID id;
    private final Operation operation;
    private final AttributeType ga;
    private double value;
    
    public AttributeModifier(final UUID uuid, final String name, final double value, final Operation operation, final AttributeType ga) {
        super();
        this.id = uuid;
        this.name = name;
        this.value = value;
        this.operation = operation;
        this.ga = ga;
    }
    
    public AttributeModifier(final UUID uuid, final AttributeType ga) {
        this(uuid, "dummy", 0.0, Operation.INCREMENT, ga);
    }
    
    public String getName() {
        return this.name;
    }
    
    public double getValue() {
        return this.value;
    }
    
    public int getOperation() {
        return this.operation.getValue();
    }
    
    public UUID getUUID() {
        return this.id;
    }
    
    public void setValue(final double value) {
        if (this.operation != Operation.INCREMENT && value < -1.0) {
            this.value = 0.0;
        }
        else {
            this.value = value;
        }
    }
    
    public AttributeType getAttribute() {
        return this.ga;
    }
    
    public void apply(final LivingEntity e) {
        AttributeBase.addModifier(e, this);
    }
    
    public ItemStack apply(final ItemStack is) {
        return AttributeItem.addModifier(this, is);
    }
    
    public void remove(final LivingEntity e) {
        AttributeBase.removeModifier(e, this);
    }
    
    public void remove(final ItemStack is) {
        AttributeItem.removeModifier(this, is);
    }
}
