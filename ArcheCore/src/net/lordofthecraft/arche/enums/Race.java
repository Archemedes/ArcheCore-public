package net.lordofthecraft.arche.enums;

public enum Race {
	HUMAN("Human", "Human", 100, 1.05),
	ELF("Elf", "Elf", 1000),
	DWARF("Dwarf", "Dwarf"),
	ORC("Uruk", "Orc"),
	KHARAJYR("Kharajyr","Kharajyr"),
	
	NORTHENER("Highlander", "Human", 100, 1.05),
	SOUTHERON("Farfolk", "Human", 100, 1.05),
	HEARTLANDER("Heartlander", "Human", 100, 1.05),
	HALFLING("Halfling", "Human", 200, 1.05),
	
	HIGH_ELF("High Elf", "Elf", 1000),
	DARK_ELF("Dark Elf", "Elf", 1000),
	WOOD_ELF("Wood Elf", "Elf", 1000),
	
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

	CONSTRUCT("Construct","Special", 10000, 0.5),
	SPECTRE("Spectre","Special", 10000, 0.0),
	NECROLYTE("Necrolyte","Special",10000),
	UNDEAD("Undead", "Special", 10000),
	ASCENDED("Ascended", "Special", 10000),
	
	UNSET("Unset", "Unset", 1000);
	
	private final String name;
	private final String supRace;
	private final int maxAge;
	private final double baseXpMultiplier;
	
	private Race(String name, String supRace){
		this(name, supRace, 500, 1d);
	}
	
	private Race(String name, String supRace, int maxAge){
		this(name, supRace, maxAge, 1d);
	}
	
	private Race(String name, String supRace, int maxAge, double baseXpMultiplier){
		this.name = name;
		this.supRace = supRace;
		this.maxAge = maxAge;
		this.baseXpMultiplier = baseXpMultiplier;
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
	
	public boolean hasChildren(){
		for (Race race : Race.values()){
			if (race.getParentRace().toUpperCase().equals(toString()))
				return true;
		}
		return false;
	}
}
