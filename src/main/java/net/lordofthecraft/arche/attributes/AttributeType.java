package net.lordofthecraft.arche.attributes;

@Deprecated
public enum AttributeType {
	MAX_HEALTH ("maxHealth", "generic.maxHealth"),
	FOLLOW_RANGE ("FOLLOW_RANGE", "generic.followRange"),
	KNOCKBACK_RESISTANCE ("c", "generic.knockbackResistance"),
	MOVEMENT_SPEED ("MOVEMENT_SPEED", "generic.movementSpeed"),
	ATTACK_DAMAGE ("ATTACK_DAMAGE", "generic.attackDamage"),
	ZOMBIE_REINFORCEMENTS("x", "zombie.spawnReinforcements"),
	HORSE_JUMPSTRENGTH("y", "horse.jumpStrength"),
	ATTACK_SPEED("f", "generic.attackSpeed"),
	ARMOR("g", "generic.Armor"),
	ARMORTOUGH("h", "generic.armorToughness"),
	LUCK("i", "generic.luck"); //1.9
	
	final String field;
	final String name;

	AttributeType(String field, String name) {
		this.field = field;
		this.name = name;
	}

	@Override
	public String toString(){
		return field;
	}
	
	public String getName(){
		return name;
	}
}
