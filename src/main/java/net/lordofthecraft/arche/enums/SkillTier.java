package net.lordofthecraft.arche.enums;

public enum SkillTier {	
	DEFAULT(0,0,"Inactive"),
	SELECTED(0,1,""),
	RENOWNED(0,100,"Renowned"),
	MASTERFUL(0,1000,"Masterful");
	
	final int xp;
	final int tier;
	final String title;

	SkillTier(int tier, int xp, String title) {
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
	 * @return this.compareTo(other) greater than or equal to 0;
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
