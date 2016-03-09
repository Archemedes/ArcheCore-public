package net.lordofthecraft.arche.enums;

public enum Race {
	HUMAN("Human", "Human", 100, 1.05, 0d),
	ELF("Elf", "Elf", 1000, 0d),
	DWARF("Dwarf", "Dwarf", 1d),
	ORC("Orc", "Orc", -1d),
	KHARAJYR("Kharajyr","Kharajyr", 1d),
	
	NORTHENER("Highlander", "Human", 100, 1.05, 0d),
	SOUTHERON("Farfolk", "Human", 100, 1.05, 0d),
	HEARTLANDER("Heartlander", "Human", 100, 1.05, 0d),
	HALFLING("Halfling", "Human", 200, 1.05, 2d),
	
	HIGH_ELF("High Elf", "Elf", 1000, 0d),
	DARK_ELF("Dark Elf", "Elf", 1000),
	WOOD_ELF("Wood Elf", "Elf", 1000),
	
	CAVE_DWARF("Cave Dwarf", "Dwarf", 1d),
	FOREST_DWARF("Forest Dwarf", "Dwarf", 1d),
	MOUNTAIN_DWARF("Mountain Dwarf", "Dwarf", 1d),
	DARK_DWARF("Dark Dwarf", "Dwarf", 1d),
	
	OLOG("Olog", "Orc", 500, -1d),
	GOBLIN("Goblin", "Orc", 2d),
	
	KHA_LEPARDA("Kha'Leparda", "Kharajyr", 1d),
	KHA_TIGRASI("Kha'Tigrasi", "Kharajyr", 1d),
	KHA_CHEETRAH("Kha'Cheetrah", "Kharajyr", 1d),
	KHA_PANTERA("Kha'Pantera", "Kharajyr", 1d),

	CONSTRUCT("Construct","Special", 10000, 0.5, 0d),
	SPECTRE("Spectre","Special", 10000, 0.0, 0d),
	NECROLYTE("Necrolyte","Special",10000, 0d),
	UNDEAD("Undead", "Special", 10000, 0d),
	ASCENDED("Aengulbound", "Special", 10000, 0d),
	
	UNSET("Unset", "Unset", 1000, 0d);
	
	private final String name;
	private final String supRace;
	private final int maxAge;
	private final double baseXpMultiplier;
    private final double luckValue;
	
	private Race(String name, String supRace){
		this(name, supRace, 500, 1d, 1d);
	}

    private Race(String name, String supRace, double luckValue) {this(name, supRace, 500, 1d, luckValue);}
	
	private Race(String name, String supRace, int maxAge){
		this(name, supRace, maxAge, 1d, 1d);
	}

    private Race(String name, String supRace, int maxAge, double luckValue){
        this(name, supRace, maxAge, 1d, luckValue);
    }
	
	private Race(String name, String supRace, int maxAge, double baseXpMultiplier, double luckValue) {
        this.name = name;
        this.supRace = supRace;
        this.maxAge = maxAge;
        this.baseXpMultiplier = baseXpMultiplier;
        this.luckValue = luckValue;
    }
	
	public String getName(){
		return name;
	}
	
	public int getMaximumAge(){
		return maxAge;
	}
	
	public String getParentRace(){
		return supRace;
	}
	
	public double getBaseXpMultiplier(){
		return baseXpMultiplier;
	}

    public double getLuckValue() { return luckValue; }
	
	public boolean hasChildren(){
		for (Race race : Race.values()){
			if (race.getParentRace().toUpperCase().equals(toString()))
				return true;
		}
		return false;
	}
}
