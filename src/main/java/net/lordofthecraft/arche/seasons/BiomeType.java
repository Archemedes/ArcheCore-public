package net.lordofthecraft.arche.seasons;

public enum BiomeType {
	UNSET(-1),
	OCEAN(0),
	PLAINS(1),
	DESERT(2),
	EXTREME_HILLS(3),
	FOREST(4),
	TAIGA(5),
	SWAMPLAND(6),
	RIVER(7),
	HELL(8),
	SKY(9),	
	FROZEN_OCEAN(10),
	FROZEN_RIVER(11),
	ICE_PLAINS(12),
	ICE_MOUNTAINS(13),
	MUSHROOM_ISLAND(14),
	MUSHROOM_SHORE(15),
	BEACH(16),
	DESERT_HILLS(17),
	FOREST_HILLS(18),
	TAIGA_HILLS(19),
	EXTREME_HILLS_EDGE(20),
	JUNGLE(21),
	JUNGLE_HILLS(22),
	JUNGLE_EDGE(23),
	DEEP_OCEAN(24),
	STONE_BEACH(25),
	COLD_BEACH(26),
	BIRCH_FOREST(27),
	BIRCH_FOREST_HILLS(28),
	ROOFED_FOREST(29),
	COLD_TAIGA(30),
	COLD_TAIGA_HILLS(31),
	MEGA_TAIGA(32),
	MEGA_TAIGA_HILLS(33),
	EXTREME_HILLS_PLUS(34),
	SAVANNA(35),
	SAVANNA_PLATEAU(36),
	MESA(37),
	MESA_PLATEAU_FOREST(38),
	MESA_PLATEAU(39),
	PLAINS_MOUNTAINS(128),
	SUNFLOWER_PLAINS(129),
	DESERT_MOUNTAINS(130),
	EXTREME_HILLS_MOUNTAINS(131),
	FLOWER_FOREST(132),
	TAIGA_MOUNTAINS(133),
	SWAMPLAND_MOUNTAINS(134),
	ICE_PLAINS_SPIKES(140),
	JUNGLE_MOUNTAINS(149),
	JUNGLE_EDGE_MOUNTAINS(151),
	BIRCH_FOREST_MOUNTAINS(155),
	BIRCH_FOREST_HILLS_MOUNTAINS(156),
	ROOFED_FOREST_MOUNTAINS(157),
	COLD_TAIGA_MOUNTAINS(158),
	MEGA_SPRUCE_TAIGA(160),
	MEGA_SPRUCE_TAIGA_HILLS(161),
	EXTREME_HILLS_PLUS_MOUNTAINS(162),	
	SAVANNA_MOUNTAINS(163),
	SAVANNA_PLATEAU_MOUNTAINS(164),
	MESA_BRYCE(165),
	MESA_PLATEAU_FOREST_MOUNTAINS(166),
	MESA_PLATEAU_MOUNTAINS(167);

	private final byte id;

	BiomeType(int i) {
		this.id = (byte) i;
	}

	public static BiomeType getBiomeFromId(byte biomeId) {

		for (BiomeType bt : BiomeType.values()) {
			if (bt.getId() == biomeId) return bt;
		}

		return UNSET;

	}

	public static BiomeType getWinterBiome(byte biomeId) {
		if (biomeId < 0) {
			biomeId += 128;
		}

		BiomeType type = BiomeType.getBiomeFromId(biomeId);

		switch (type) {
		case OCEAN:
		case DEEP_OCEAN: 
			return FROZEN_OCEAN;

		case BEACH:
		case STONE_BEACH:
			return COLD_BEACH;

		case BIRCH_FOREST:
		case FLOWER_FOREST:
		case FOREST:		
		case MEGA_SPRUCE_TAIGA:
		case ROOFED_FOREST:
		case SWAMPLAND:
		case MEGA_TAIGA:
		case TAIGA:
			return COLD_TAIGA;

		case BIRCH_FOREST_HILLS:
		case FOREST_HILLS:
		case EXTREME_HILLS:
		case MEGA_SPRUCE_TAIGA_HILLS:
		case EXTREME_HILLS_EDGE:	
		case EXTREME_HILLS_PLUS:
		case MEGA_TAIGA_HILLS:
		case TAIGA_HILLS:
			return COLD_TAIGA_HILLS;

		case BIRCH_FOREST_HILLS_MOUNTAINS:		
		case BIRCH_FOREST_MOUNTAINS:
		case TAIGA_MOUNTAINS:
		case EXTREME_HILLS_MOUNTAINS:
		case EXTREME_HILLS_PLUS_MOUNTAINS:
		case SWAMPLAND_MOUNTAINS:
		case ROOFED_FOREST_MOUNTAINS:
			return COLD_TAIGA_MOUNTAINS;

		case PLAINS:
		case SUNFLOWER_PLAINS:
		case PLAINS_MOUNTAINS:
			return ICE_PLAINS;

		case RIVER:
			return FROZEN_RIVER;

		default:
			return UNSET;
		}
	}

	public byte getId() {
		return id;
	}
}