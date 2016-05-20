package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.enums.Race;
import org.bukkit.Material;

public interface SkillFactory {

	/**
	 * Sets the Visbility strategy of this skill, as defined by the static constants in the Skill class.
	 * This value is set as VISIBLE (1) by default
	 * <p>
	 * Skills can me made visible or kept invisible to Personas, or 'revealed' based on certain conditions.
	 * @param visibility The level of visibility
	 * @return itself
	 */
	SkillFactory withVisibilityType(int visibility);

	/**
	 * Set this skill as inert (false by default).
	 * <p>
	 * An inert skill can be trained (Xp is gained, provided all other conditions for xp gain are met),
	 * whether or not the skill is currently visible to a Persona.
	 * @param inert If the skill can have experience added
	 * @return itself
	 */
	SkillFactory withXpGainWhileHidden(boolean inert);

	/**
	 * A racial profession will allow players of this race to level the skill to maximum level no matter what. This method can be chained multiple times.
	 * @param race The race to set this profession for.
	 * @return itself
	 */
	SkillFactory asRacialProfession(Race race);
	
	/**
	 * Add an XP modifier that is dependent on the Persona's race. All XP gained will be multiplied by this provided factor.
	 * XP lost is multiplied by the reciprocal of this factor, although XP lost through the forgettable mechanic is not modified
	 * @param race The race for which to modify all gained XP.
	 * @param modifier The factor to modify by
	 * @return itself
	 */
	SkillFactory withRacialModifier(Race race, double modifier);

	/**
	 * Lets you provide an accessible help file with your skill, to teach players how to use your skill.
	 * A help file is always visible, regardless of visibility of your skill.
	 * @param helpText The text to output when the Help File for this skill is requested
	 * @param helpIcon The Item icon to display in the Help Inventory Menu.
	 * @return itself
	 */
	SkillFactory withHelpFile(String helpText, Material helpIcon);
	
	/**
	 * Defaults to false.
	 * An intensive skill can only be set as the primary profession and selecting it will act as if
	 * both a primary and secondary profession are set, capping xp accordingly and disallowing a secondary profession
	 * an additional profession can still be set, however.
	 *
	 * @param intensive If the profession will be intensive (take two skill slots instead of one)
	 * @return itself
	 */
	SkillFactory setIntensiveProfession(boolean intensive);

	/**
	 * Sets whether or not the skill will be unlocked when a user has reached a certain number of minutes on a persona.
	 * @see Skill#ALL_SKILL_UNLOCK_TIME
	 * @param unlockByTime If the skill will be unlocked at the stated time.
	 * @return itself
	 */
	SkillFactory setUnlockedByTime(boolean unlockByTime);

	/**
	 * Process the SkillFactory object to create a new skill. This method
	 * will automatically do all the required back-end work to make the skill
	 * immediately accessible and usable as per your used settings.
	 * @return The new Skill object, registered with your provided settings.
	 */
	Skill register();



}