package net.lordofthecraft.arche.enums;

import org.bukkit.ChatColor;
import org.bukkit.Material;

public enum AbilityScore {
    CONSTITUTION("constitution", ChatColor.DARK_RED, "\u2665", "Constitution", Material.DIAMOND_CHESTPLATE, true,
            "Constitution is the hardiness of a persona. Personas of a higher constitution are more resistant to poison, disease, and all manner of negative health effects."),
    STRENGTH("strength", ChatColor.RED, "\u2694", "Strength", Material.IRON_SWORD, true,
            "Strength is the raw physical might of a Persona. Personas of a higher strength can lift heavier objects and use heavier equipment."),
    DEXTERITY("dexterity", ChatColor.GREEN, "\u21A3", "Dexterity", Material.BOW, true,
            "Dexterity is the Agileness of a Persona. Personas of a higher Dexterity find movements easier and find lighter weapons easier to use"),
    INTELLECT("intellect", ChatColor.BLUE, "\u2735", "Intellect", Material.BOOK_AND_QUILL, true,
            "Intellect is the representation of the capacity of knowledge for a Persona. Personas of a higher intellect learn quicker and are more capable of casting spells"),
    WISDOM("wisdom", ChatColor.AQUA, "\u2697", "Wisdom", Material.TORCH, true,
            "Wisdom is the measurement of a Personas perception and will. Personas of a higher wisdom have strong, hard to break wills and are more keen to happenings in their environment"),
    LUCK("luck", ChatColor.DARK_GREEN, "\u2663", "Luck", Material.RABBIT_FOOT, true,
            "Luck is the measurement of a Persona's good fortune. Personas of higher luck are, well, luckier."),
    UNSPENT("unspent", ChatColor.WHITE, "", "Unspent", Material.NETHER_STAR, false, "Points remaining to spend");

    private final String field;
    private final ChatColor color;
    private final String icon;
    private final String name;
    private final Material itemIcon;
    private final boolean changeable;
    private final String desc;

    AbilityScore(String field, ChatColor color, String icon, String name, Material itemIcon, boolean changeable, String lore) {
        this.field = field;
        this.color = color;
        this.icon = icon;
        this.name = name;
        this.itemIcon = itemIcon;
        this.changeable = changeable;
        this.desc = lore;
    }

    public String field() {
        return field;
    }

    public char getSymbol() {
        return icon.charAt(0);
    }

    public String getIcon() {
        return color + icon;
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public Material getItemIcon() {
        return itemIcon;
    }

    public String getStringID() {
        return getIcon() + color + " " + name;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isChangeable() {
        return changeable;
    }
}
