package net.lordofthecraft.arche.enums;

public enum Race
{
    HUMAN("Human", "Human", 100, 1.05), 
    ELF("Elf", "Elf", 1000), 
    DWARF("Dwarf", "Dwarf"), 
    ORC("Uruk", "Orc"), 
    KHARAJYR("Kharajyr", "Kharajyr"), 
    NORTHENER("Highlander", "Human", 100, 1.05), 
    SOUTHERON("Farfolk", "Human", 100, 1.05), 
    EASTERNER("Easterner", "Human", 100, 1.05), 
    HALFLING("Halfling", "Human", 200, 1.05), 
    HIGH_ELF("High Elf", "Elf", 1000), 
    DARK_ELF("Dark Elf", "Elf", 1000), 
    WOOD_ELF("Wood Elf", "Elf", 1000), 
    CAVE_DWARF("Cave Dwarf", "Dwarf"), 
    FOREST_DWARF("Forest Dwarf", "Dwarf"), 
    MOUNTAIN_DWARF("Mountain Dwarf", "Dwarf"), 
    DARK_DWARF("Dark Dwarf", "Dwarf"), 
    OLOG("Olog", "Orc"), 
    GOBLIN("Goblin", "Orc"), 
    KHA_LEPARDA("Kha'Leparda", "Kharajyr"), 
    KHA_TIGRASI("Kha'Tigrasi", "Kharajyr"), 
    KHA_CHEETRAH("Kha'Cheetrah", "Kharajyr"), 
    KHA_PANTERA("Kha'Pantera", "Kharajyr"), 
    UNDEAD("Undead", "Noble", 10000), 
    ASCENDED("Ascended", "Noble", 10000), 
    UNSET("Unset", "Unset", 1000);
    
    private final String name;
    private final String supRace;
    private final int maxAge;
    private final double baseXpMultiplier;
    
    private Race(final String name, final String supRace) {
        this(name, supRace, 500, 1.0);
    }
    
    private Race(final String name, final String supRace, final int maxAge) {
        this(name, supRace, maxAge, 1.0);
    }
    
    private Race(final String name, final String supRace, final int maxAge, final double baseXpMultiplier) {
        this.name = name;
        this.supRace = supRace;
        this.maxAge = maxAge;
        this.baseXpMultiplier = baseXpMultiplier;
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getMaximumAge() {
        return this.maxAge;
    }
    
    public String getParentRace() {
        return this.supRace;
    }
    
    public double getBaseXpMultiplier() {
        return this.baseXpMultiplier;
    }
}
