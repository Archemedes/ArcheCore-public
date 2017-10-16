package net.lordofthecraft.arche.enums;

import org.bukkit.ChatColor;

public enum AbilityScore {
    CONSTITUTION("constitution", ChatColor.DARK_RED+"\u1F5A4"),
    STRENGTH("strength", ChatColor.RED+"\u1F4AA"),
    DEXTERITY("dexterity", ChatColor.GREEN+"\u1F3F9"),
    INTELLECT("intellect", ChatColor.BLUE+"\u1F4DC"),
    WISDOM("wisdom", ChatColor.AQUA+"\u2735"),
    LUCK("luck", ChatColor.DARK_GREEN+"\u2663");

    private final String field;
    private final String icon;

    AbilityScore(String field, String icon) {
        this.field = field;
        this.icon = icon;
    }

    public String field() {
        return field;
    }

    public char getSymbol() {
        return icon.charAt(2);
    }

    public String getIcon() {
        return icon;
    }
}
