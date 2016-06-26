package net.lordofthecraft.arche.attributes;

@Deprecated
public enum AttributeSlot {
    MAINHAND("mainhand"),
    OFFHAND("offhand"),
    FEET("feet"),
    LEGS("legs"),
    CHEST("chest"),
    HEAD("head");

    private final String value;

    AttributeSlot(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return value;
    }
}
