package net.lordofthecraft.arche.enums;

public enum Race {
    HUMAN("Human", "Human", 100),
    ELF("Elf", "Elf", 1000),
    DWARF("Dwarf", "Dwarf"),
    ORC("Orc", "Orc"),
    KHARAJYR("Kharajyr", "Kharajyr"),

    NORTHENER("Highlander", "Human", 100),
    SOUTHERON("Farfolk", "Human", 100),
    HEARTLANDER("Heartlander", "Human", 100),
    ADUNIAN("Adunian", "Human", 200),
    HALFLING("Halfling", "Human", 200),

    HIGH_ELF("High Elf", "Elf", 1000),
    DARK_ELF("Dark Elf", "Elf", 1000),
    WOOD_ELF("Wood Elf", "Elf", 1000),
    SNOW_ELF("Snow Elf", "Elf", 1000), //Kill me please -501warhead

    CAVE_DWARF("Cave Dwarf", "Dwarf"),
    FOREST_DWARF("Forest Dwarf", "Dwarf"),
    MOUNTAIN_DWARF("Mountain Dwarf", "Dwarf"),
    DARK_DWARF("Dark Dwarf", "Dwarf"),

    OLOG("Olog", "Orc", 500),
    GOBLIN("Goblin", "Orc"),

    KHA_LEPARDA("Kha'Leparda", "Kharajyr"),
    KHA_TIGRASI("Kha'Tigrasi", "Kharajyr"),
    KHA_CHEETRAH("Kha'Cheetrah", "Kharajyr"),
    KHA_PANTERA("Kha'Pantera", "Kharajyr"),

    CONSTRUCT("Construct", "Creature", 10000),
    SPECTRE("Spectre", "Creature", 10000),
    NECROLYTE("Necrolyte", "Creature", 10000),
    NEPHILIM("Azdrazi", "Creature", 10000),
    UNDEAD("Undead", "Special", 10000),
    ASCENDED("Aengulbound", "Special", 10000),
    AENGUL("Aengul", "Special", 10000),
    DAEMON("Daemon", "Special", 10000),

    UNSET("Unset", "Unset", 1000);

    private final String name;
    private final String supRace;
    private final int maxAge;

    Race(String name, String supRace) {
        this(name, supRace, 500);
    }

    Race(String name, String supRace, int maxAge) {
        this.name = name;
        this.supRace = supRace;
        this.maxAge = maxAge;
    }

    public String getName() {
        return name;
    }

    public int getMaximumAge() {
        return maxAge;
    }

    public String getParentRace() {
        return supRace;
    }

    public boolean hasChildren() {
        for (Race race : Race.values()) {
            if (race.getParentRace().toUpperCase().equals(toString()))
                return true;
        }
        return false;
    }
}
