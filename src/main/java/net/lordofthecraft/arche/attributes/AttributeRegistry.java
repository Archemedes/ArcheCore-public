package net.lordofthecraft.arche.attributes;

import com.google.common.collect.Sets;
import org.bukkit.attribute.Attribute;

import java.util.Optional;
import java.util.Set;

public class AttributeRegistry {

    public static final ArcheAttribute MAX_FATIGUE = new AttributeMaxFatigue("Maximum Fatigue", 100.0);
    public static final ArcheAttribute FATIGUE_GAIN = new ArcheAttribute("Fatigue Gained", 1);
    public static final ArcheAttribute EXHAUSTION = new ArcheAttribute("Exhaustion", 0);

    static {
        getInstance().register(MAX_FATIGUE);
        getInstance().register(FATIGUE_GAIN);
        getInstance().register(EXHAUSTION);
        for (Attribute a : Attribute.values()) {
            getInstance().register(new VanillaAttribute(a.toString(), 0.0, a));
        }
    }

    private static AttributeRegistry ourInstance = new AttributeRegistry();

    public static AttributeRegistry getInstance() {
        return ourInstance;
    }

    private AttributeRegistry() {
    }

    public Set<ArcheAttribute> registeredAttributes = Sets.newConcurrentHashSet();

    public void register(ArcheAttribute attr) {
        if (registeredAttributes.contains(attr)) {
            return;
        }
        registeredAttributes.add(attr);
    }

    public Optional<VanillaAttribute> getVanillaAttribute(Attribute attribute) {
        return registeredAttributes.stream()
                .filter(a -> a.getName().equalsIgnoreCase(attribute.toString()) && a instanceof VanillaAttribute)
                .map(m -> (VanillaAttribute) m)
                .findFirst();
    }

    public Optional<ArcheAttribute> getAttribute(String name) {
        return registeredAttributes.stream().filter(a -> a.getName().equals(name)).findFirst();
    }

}
