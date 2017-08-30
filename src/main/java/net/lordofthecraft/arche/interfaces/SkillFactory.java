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
	 * A racial profession will allow players to have this skill even if they don't select it as their main profession.
	 * @param race The race to set this profession for.
	 * @return itself
	 */
	SkillFactory asRacialProfession(Race race);
	
	/**
	 * Add an Fatigue modifier that is dependent on the Persona's race. Values higher than 1 are beneficial for the player.
	 * @param race The race for which to modify all gained fatigue.
	 * @param modifier The effective factor by which to modify the total fatigue a player can accumulate before losing skill benefits
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
	 * Process the SkillFactory object to create a new skill. This method
	 * will automatically do all the required back-end work to make the skill
	 * immediately accessible and usable as per your used settings.
	 * @return The new Skill object, registered with your provided settings.
	 */
	Skill register();



}