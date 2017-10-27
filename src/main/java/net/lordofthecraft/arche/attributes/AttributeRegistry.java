package net.lordofthecraft.arche.attributes;

import com.google.common.collect.Sets;
import net.lordofthecraft.arche.enums.AbilityScore;
import org.bukkit.attribute.Attribute;

import java.util.Set;

public class AttributeRegistry {

    public static final ArcheAttribute MAX_FATIGUE = new AttributeMaxFatigue("Maximum Fatigue", 100.0);
    public static final ArcheAttribute FATIGUE_GAIN = new ArcheAttribute("Fatigue Gained", 1, false);
    public static final ArcheAttribute EXHAUSTION = new ArcheAttribute("Exhaustion", 0, false);
    public static final ArcheAttribute ARROW_DAMAGE = new ArcheAttribute("Archery Damage", 1.0);
    public static final ArcheAttribute ARROW_VELOCITY = new ArcheAttribute("Arrow Velocity", 1.0);
    public static final ArcheAttribute SCORE_CONSTITUTION = new AttributeAbilityScore(AbilityScore.CONSTITUTION, 0.0);
    public static final ArcheAttribute SCORE_STRENGTH = new AttributeAbilityScore(AbilityScore.STRENGTH, 0.0);
    public static final ArcheAttribute SCORE_DEXTERITY = new AttributeAbilityScore(AbilityScore.DEXTERITY, 0.0);
    public static final ArcheAttribute SCORE_INTELLECT = new AttributeAbilityScore(AbilityScore.INTELLECT, 0.0);
    public static final ArcheAttribute SCORE_WISDOM = new AttributeAbilityScore(AbilityScore.WISDOM, 0.0);
    public static final ArcheAttribute SCORE_LUCK = new AttributeAbilityScore(AbilityScore.LUCK, 0.0);

    public static final ArcheAttribute SCORE_CAP_CONSTITUTION = new AttributeAbilityScoreCap(AbilityScore.CONSTITUTION, 10.0);
    public static final ArcheAttribute SCORE_CAP_STRENGTH = new AttributeAbilityScoreCap(AbilityScore.STRENGTH, 10.0);
    public static final ArcheAttribute SCORE_CAP_DEXTERITY = new AttributeAbilityScoreCap(AbilityScore.DEXTERITY, 10.0);
    public static final ArcheAttribute SCORE_CAP_INTELLECT = new AttributeAbilityScoreCap(AbilityScore.INTELLECT, 10.0);
    public static final ArcheAttribute SCORE_CAP_WISDOM = new AttributeAbilityScoreCap(AbilityScore.WISDOM, 10.0);
    public static final ArcheAttribute SCORE_CAP_LUCK = new AttributeAbilityScoreCap(AbilityScore.LUCK, 10.0);


    private static AttributeRegistry INSTANCE = new AttributeRegistry();

    public static AttributeRegistry getInstance() {
        return INSTANCE;
    }

    private AttributeRegistry() {
        register(MAX_FATIGUE);
        register(FATIGUE_GAIN);
        register(EXHAUSTION);
        register(ARROW_DAMAGE);
        register(ARROW_VELOCITY);

        register(SCORE_CONSTITUTION);
        register(SCORE_STRENGTH);
        register(SCORE_DEXTERITY);
        register(SCORE_INTELLECT);
        register(SCORE_WISDOM);
        register(SCORE_LUCK);

        register(SCORE_CAP_CONSTITUTION);
        register(SCORE_CAP_STRENGTH);
        register(SCORE_CAP_DEXTERITY);
        register(SCORE_CAP_INTELLECT);
        register(SCORE_CAP_WISDOM);
        register(SCORE_CAP_LUCK);

        for (Attribute a : Attribute.values()) {
            register(new VanillaAttribute(a.toString(), 0.0, a));
        }
    }

    public Set<ArcheAttribute> registeredAttributes = Sets.newConcurrentHashSet();

    public void register(ArcheAttribute attr) {
        if (registeredAttributes.contains(attr)) {
            return;
        }
        registeredAttributes.add(attr);
    }

    public VanillaAttribute getVanillaAttribute(Attribute attribute) {
        return registeredAttributes.stream()
                .filter(a -> a.getName().equalsIgnoreCase(attribute.toString()) && a instanceof VanillaAttribute)
                .map(m -> (VanillaAttribute) m)
                .findFirst().orElse(null);
    }

    public ArcheAttribute getAttribute(String name) {
        return registeredAttributes.stream().filter(a -> a.getName().equals(name)).findFirst().orElse(null);
    }

    public static VanillaAttribute getSVanillaAttribute(Attribute attribute) {
        return INSTANCE.getVanillaAttribute(attribute);
    }

    public static ArcheAttribute getSAttribute(String name) {
        return INSTANCE.getAttribute(name);
    }

}
