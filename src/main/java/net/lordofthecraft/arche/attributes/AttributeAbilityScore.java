package net.lordofthecraft.arche.attributes;

import net.lordofthecraft.arche.enums.AbilityScore;

public class AttributeAbilityScore extends ArcheAttribute {

    private final AbilityScore score;

    public AttributeAbilityScore(AbilityScore score, double defaultValue) {
        super(score.getName(), defaultValue);
        this.score = score;
    }

    public AttributeAbilityScore(AbilityScore score, double defaultValue, boolean higherIsBetter) {
        super(score.getName(), defaultValue, higherIsBetter);
        this.score = score;
    }
}
