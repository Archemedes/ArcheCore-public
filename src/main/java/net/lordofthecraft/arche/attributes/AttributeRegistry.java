package net.lordofthecraft.arche.attributes;

import org.apache.commons.lang.Validate;
import org.bukkit.attribute.Attribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AttributeRegistry {

    public static final ArcheAttribute MAX_FATIGUE = new AttributeMaxFatigue("Maximum Fatigue", 100.0);
    public static final ArcheAttribute FATIGUE_GAIN = new ArcheAttribute("Fatigue Gained", 1, false);
    public static final ArcheAttribute EXHAUSTION = new ArcheAttribute("Exhaustion", 0, false);
    public static final ArcheAttribute MOB_DAMAGE_DEAL = new ArcheAttribute("Damage to Mobs", 1.0);
    public static final ArcheAttribute MOB_DAMAGE_TAKE = new ArcheAttribute("Damage from Mobs", 1.0, false);
    public static final ArcheAttribute REGENERATION = new ArcheAttribute("Regeneration", 1.0);
    public static final ArcheAttribute HUNGER = new ArcheAttribute("Hunger", 1.0, false);
    public static final ArcheAttribute ARROW_DAMAGE = new ArcheAttribute("Archery Damage", 1.0);
    public static final ArcheAttribute ARROW_VELOCITY = new ArcheAttribute("Arrow Velocity", 1.0);
    public static final ArcheAttribute SHROUD = new ArcheAttribute("Shroud", 0);
    public static final ArcheAttribute PERCEPTION = new ArcheAttribute("Perception", 0);
    
    //Resistance attributes
    public static final ArcheAttribute POISON_RESISTANCE = new ArcheAttribute("Poison Resistance", 1.0);
    public static final ArcheAttribute FIRE_RESISTANCE = new ArcheAttribute("Fire Resistance", 1.0);
    public static final ArcheAttribute WITHER_RESISTANCE = new ArcheAttribute("Wither Resistance", 1.0);
    public static final ArcheAttribute MAGIC_RESISTANCE = new ArcheAttribute("Magic Resistance", 1.0);
    public static final ArcheAttribute DROWNING_RESISTANCE = new ArcheAttribute("Drowning Resistance", 1.0);
    public static final ArcheAttribute BLAST_RESISTANCE = new ArcheAttribute("Blast Resistance", 1.0);
    public static final ArcheAttribute PROJECTILE_RESISTANCE = new ArcheAttribute("Projectile Resistance", 1.0);
    public static final ArcheAttribute LIGHTNING_RESISTANCE = new ArcheAttribute("Lightning Resistance", 1.0);

    private static AttributeRegistry INSTANCE = new AttributeRegistry();
    public Map<String, ArcheAttribute> registeredAttributes = new HashMap<>();
    
    public static AttributeRegistry getInstance() {
        return INSTANCE;
    }

    private AttributeRegistry() {
        register(MAX_FATIGUE);
        register(FATIGUE_GAIN);
        register(EXHAUSTION);
        register(MOB_DAMAGE_DEAL);
        register(MOB_DAMAGE_TAKE);
        register(REGENERATION);
        register(HUNGER);
        register(ARROW_DAMAGE);
        register(ARROW_VELOCITY);
        register(SHROUD);
        register(PERCEPTION);
        
        register(POISON_RESISTANCE);
        register(FIRE_RESISTANCE);
        register(WITHER_RESISTANCE);
        register(MAGIC_RESISTANCE);
        register(DROWNING_RESISTANCE);
        register(BLAST_RESISTANCE);
        register(PROJECTILE_RESISTANCE);
        register(LIGHTNING_RESISTANCE); 
        
        for (Attribute a : Attribute.values()) {
            register(new VanillaAttribute(a.toString(), 0.0, a));
        }
    }

    public Map<String, ArcheAttribute> getAttributes() {
    	return Collections.unmodifiableMap(registeredAttributes);
    }

    public void register(ArcheAttribute attr) {
        Validate.isTrue(!registeredAttributes.containsKey(attr.getName()), "Conflicting attribute name being registered: " + attr.getName());
        registeredAttributes.put(attr.getName(), attr);
    }

    public VanillaAttribute getVanillaAttribute(Attribute attribute) {
    	return (VanillaAttribute) registeredAttributes.get(attribute.toString());
    }

    public ArcheAttribute getAttribute(String name) {
        return registeredAttributes.get(name);
    }

    public static VanillaAttribute getSVanillaAttribute(Attribute attribute) {
        return INSTANCE.getVanillaAttribute(attribute);
    }

    public static ArcheAttribute getSAttribute(String name) {
        return INSTANCE.getAttribute(name);
    }

}
