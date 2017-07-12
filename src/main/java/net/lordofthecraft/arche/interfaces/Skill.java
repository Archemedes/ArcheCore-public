package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.enums.SkillTier;
import net.lordofthecraft.arche.persona.Race;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;

public interface Skill {

	/**
	 * Represents the required time in minutes for a persona to unlock the ability to level all skills to the maximum tier. Currently 200 days.
	 */
	int ALL_SKILL_UNLOCK_TIME = 288000;

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
	 * Fetch the Name of the Skill
	 * @return name of the Skill
	 */
	String getName();

	/**
	 * Gain the session-dependent internal id of the skill
	 * @return id of the skill
	 */
	int getId();

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
	 * @param modify Whether or not to modify experience with modifiers
	 * @return the amount added
	 */
	double addRawXp(Player p, double xp, boolean modify);

	double addRawXp(Persona p, double xp);

	/**
	 * Method to add XP to a Persona. This method will add XP whether or not XP gain
	 * is turned off and does not deduce XP from other skills. Hidden skills will not 
	 * have xp added, unless adding xp reveals this skill (Discoverable)
	 * @param p The Persona in question
	 * @param xp Amount of XP to add
	 * @param modify whether or not to apply XP modifiers (such as Racial bonuses)
     * @return the amount added
	 */
	double addRawXp(Persona p, double xp, boolean modify);

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
	 * Return boolean not affected if Persona XP gain was turned off by the Player.
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
	 * Persona's are limited in their maximum proficiency based on the professions selected. The maximum can be checked with this method
	 * @param p The Persona in question
	 * @return The maximum SkillTier obtainable by this Persona
	 */
	SkillTier getCapTier(Persona p);

	/**
	 * An intensive skill can only be set as the primary profession and selecting it will act as if
	 * both a primary and secondary profession are set, capping xp accordingly and disallowing a secondary profession
	 * an additional profession can still be set, however
	 * @return if the skill is intensive.
	 */
	boolean isIntensiveProfession();

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