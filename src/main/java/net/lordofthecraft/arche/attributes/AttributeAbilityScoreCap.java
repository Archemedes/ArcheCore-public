package net.lordofthecraft.arche.attributes;

import net.lordofthecraft.arche.enums.AbilityScore;

public class AttributeAbilityScoreCap extends ArcheAttribute {

    private final AbilityScore score;

    public AttributeAbilityScoreCap(AbilityScore score, double defaultValue) {
        super("cap_" + score.getName(), defaultValue);
        this.score = score;
    }

    public AttributeAbilityScoreCap(AbilityScore score, double defaultValue, boolean higherIsBetter) {
        super("cap_" + score, defaultValue, higherIsBetter);
        this.score = score;
    }
}
