package net.lordofthecraft.arche.enums;

public enum SkillTier {
/*	RUSTY(0,Integer.MIN_VALUE,"Inept"),
	INEXPERIENCED(1,-3000,"Promising"),
	BUNGLING(2,250,"Bungling"),
	CLUMSY(3, 600, "Clumsy"),
	APPRENTICE(4,1500,"Apprentice"),
	FAIR(5,3000,"Fair"),
	ADEQUATE(6,6500,"Adequate"),
	JOURNEYMAN(7, 14000, "Journeyman"),
	PRACTICED(8, 30000, "Practiced"),
	PROFICIENT(9, 70000,"Proficient"),
	ADEPT(10, 150000,"Adept"),
	EXCELLENT(11, 350000, "Excellent"),
	VETERAN(12, 1000000 ,"Veteran"),
	MASTERFUL(13, 3000000,"Masterful"),
	LEGENDARY(14, 10000000,"Legendary"),
	AENGULIC(15, 35000000,"Aengulic");*/
	
	RUSTY(0,Integer.MIN_VALUE,"Inept"),
	INEXPERIENCED(1,-3000,"Promising"),
	BUNGLING(2,250,"Bungling"),
	CLUMSY(3, 600, "Clumsy"),
	APPRENTICE(4,1500,"Apprentice"),
	FAIR(5,3000,"Fair"),
	ADEQUATE(6,6500,"Adequate"),
	PROFICIENT(7, 14000,"Proficient"),
	ADEPT(8, 30000,"Adept"),
	VETERAN(9, 70000,"Veteran"),
	MASTERFUL(10, 150000,"Masterful"),
	LEGENDARY(11, 350000,"Legendary"),
	AENGULIC(12, 1000000,"Aengulic");
	
	final int xp;
	final int tier;
	final String title;
	
	private SkillTier(int tier, int xp, String title){
		this.xp = xp;
		this.tier = tier;
		this.title = title;
	}
	
	/**
	 * Get the human-readable title for this Tier
	 * @return the tier's title
	 */
	public String getTitle(){
		return title;
	}
	
	/**
	 * See if a persona, being of this particular skill tier, has achieved a certain provided tier.
	 * @param other the SkillTier to compare to
	 * @return this.compareTo(other) >= 0;
	 */
	public boolean achieved(SkillTier other){
		return this.compareTo(other) >= 0;
	}
	
	/**
	 * Retrieve the Skill Tier that succeeds the current one, or in
	 * other words, the Tier the player will reach on their next level up. 
	 * @return The next Tier
	 */
	public SkillTier getNext(){
		return this.ordinal() < SkillTier.values().length - 1
				? SkillTier.values()[this.ordinal() + 1]
		        : null;
	}
	
	/**
	 * Get the numerical representation of this skill tier. Numbers are increasing integers where a higher integer
	 * means a higher XP treshold for that partcular tier
	 * @return The tier's associated integer.
	 */
	public int getTier(){
		return tier;
	}
	
	/**
	 * Retrieve the XP treshold that is required for a player to achieve
	 * this given tier. Any Player with experience equal or greater than this
	 * treshold will have achieved this tier.
	 * @return The XP needed to be this tier.
	 */
	public int getXp(){
		return xp;
	}

}
