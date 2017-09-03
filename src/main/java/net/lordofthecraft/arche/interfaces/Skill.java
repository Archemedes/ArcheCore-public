package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.enums.SkillTier;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

public interface Skill {
	/**
	 * Skill is Visible
	 */
	int VISIBILITY_VISIBLE = 1;
	/**
	 * Skill is invisible until the Perona gains XP in it.
	 */
	int VISIBILITY_DISCOVERABLE = 2;
	/**
	 * Skill is invisible until the Persona applied a Teacher or Tome.
	 */
	int VISIBILITY_GRANTABLE = 3;
	/**
	 * Skill is always invisible (unless forcibly revealed via command)
	 */
	int VISIBILITY_HIDDEN = 4;

	/**
	 * Fetch the human-readable name of the act of practicing a skill
	 * @return name of the Skill
	 */
	String getName();
	
	/**
	 * Gets a human-readable name of a professional for this skill
	 * If this has not been set at creation, returns {@link #getName()} instead
	 * @return noun of person that practices this skill
	 */
	String getProfessionalName();
	
	/**
	 * Gets a human-readable name of a professional for this skill
	 * If this has not been set at creation, returns {@link #getName()} instead
	 * @param female should be for a female character?
	 * @return noun of person that practices this skill
	 */
	String getProfessionalName(boolean female);

	/**
	 * Retrieves the Visbility strategy of this skill, as defined by the static constants in this class.
	 * Skills can me made visible or kept invisible to Personas, or 'revealed' based on certain conditions.
	 * @return The visibility strategy of the skill
	 */
	int getVisibility();

	/**
	 * An inert skill can be trained (Xp is gained, provided all other conditions for xp gain are met),
	 * whether or not the skill is currently visible to a Persona.
	 * @return whether or not this skill is inert.
	 */
	boolean isInert();

	/**
	 * Test if this skill is currently visible to the Player's current persona. 
	 * @param p The Player to test for
	 * @return Visibility of the skill.
	 */
	boolean isVisible(Player p);

	/**
	 * Test if this skill is currently visible to the given Persona. 
	 * @param p The Persona to test for
	 * @return Visibility of the skill.
	 */
	boolean isVisible(Persona p);

	/**
	 * Force to reveal the skill to the given Persona, after which 
	 * it is treated as 'visible' for that Persona.
	 * @param p The Persona to reveal for.
	 * @return whether or not this skill was already visible.
	 */
	boolean reveal(Persona p);

	/**
	 * Method to add XP to a Player's current Persona, provided the Player is able to gain XP in this skill.
	 * Calling this method also reduces the XP of all skills, if and where applicable.
		 * @param p The Player in question
	 * @param xp Amount of XP to add
	 */
	void addXp(Player p, double xp);

	/**
	 * Method to add XP to a Persona, provided the Player is able to gain XP in this skill.
	 * Calling this method also reduces the XP of all skills, if and where applicable.
	 * @param p The Persona in question
	 * @param xp Amount of XP to add
	 */
	void addXp(Persona p, double xp);

	/**
	 * Method to add XP to a Persona. This method will add XP whether or not XP gain
	 * is turned off and does not deduce XP from other skills. Hidden skills will not 
	 * have xp added, unless adding xp reveals this skill (Discoverable)
	 * @param p The Player in question
	 * @param xp Amount of XP to add
	 * @return the amount added
	 */
	double addRawXp(Player p, double xp);

	/**
	 * Method to add XP to a Persona. This method will add XP whether or not XP gain
	 * is turned off and does not deduce XP from other skills. Hidden skills will not 
	 * have xp added, unless adding xp reveals this skill (Discoverable)
	 * @param p The Persona in question
	 * @param xp Amount of XP to add
	 * @return the amount added
	 */
	double addRawXp(Persona p, double xp);

	/**
	 * Method to retrieve the xp in this skill for the Player's current Persona
	 * @param p The Player in question
	 * @return the amount of XP the Persona has accumulated in a skill
	 */
	double getXp(Player p);

	/**
	 * Method to retrieve the xp in this skill for the Persona
	 * @param p The Persona in question
	 * @return the amount of XP the Persona has accumulated in a skill
	 */
	double getXp(Persona p);

	/**
	 * See whether or not the tier of the Player's current Persona is AT LEAST the provided tier
	 * @param p The Player in question
	 * @param tier The tier the Persona must have or exceed
	 * @return Whether or not the Persona has at least this tier in this skill.
	 */
	boolean achievedTier(Player p, SkillTier tier);

	/**
	 * See whether or not the tier of the Persona is AT LEAST the provided tier
	 * @param p The Persona in question
	 * @param tier The tier the Persona must have or exceed
	 * @return Whether or not the Persona has at least this tier in this skill.
	 */
	boolean achievedTier(Persona p, SkillTier tier);

	/**
	 * Retrieve the tier the Player's current Persona is on.
	 * @param p The Player in question
	 * @return The Skill Tier of the Persona
	 */
	SkillTier getSkillTier(Player p);

	/**
	 * Retrieve the tier the Persona is on.
	 * @param p The Persona in question
	 * @return The Skill Tier of the Persona
	 */
	SkillTier getSkillTier(Persona p);

	/**
	 * See if the Persona can gain XP in this skill.
	 * Returned boolean not affected if Persona XP gain was turned off by the Player.
	 * @param p The Persona in question
	 * @return If the Player can currently gain XP in this skill.
	 */
	boolean canGainXp(Persona p);

	/**
	 * A main profession can always be levelled to max by players of this race
	 * @param race The race to check for
	 * @return Whether or not this Race has this Skill for a profession.
	 */
	boolean isProfessionFor(Race race);

	/**
	 * Resets a skill to zero xp, assuming the skill has more than zero xp currently.
	 * @param p The Persona to reset for.
	 * @return Amount of XP that was drained
	 */
	double reset(Persona p);

	/**
	 * @return The list of registered racial mods for this skill.
	 */
    Map<net.lordofthecraft.arche.persona.Race, Double> getRaceMods();

	/**
	 * @return The list of races who call this skill their racial profession, or one of them.
	 */
    Set<net.lordofthecraft.arche.persona.Race> getMains();
}