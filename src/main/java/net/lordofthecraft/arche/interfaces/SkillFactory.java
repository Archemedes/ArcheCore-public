package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.enums.Race;
import org.bukkit.Material;

public interface SkillFactory {

	/**
	 * A noun for a practitioner of this particular skill
	 * @param name Noun to use for male/default professional
	 * @return itself
	 */
	SkillFactory withProfessionalName(String name);
	
	/**
	 * A noun for a female practitioner of this particular skill
	 * @param name Noun to use for female professional
	 * @return itself
	 */
	SkillFactory withFemaleProfessionalName(String name);
	
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
     * Skills, once created once, will be saved in SQL and loaded in {@link ArcheCore#onEnable()}
     * After this has completed skills will be loaded from SQL and will be in a read state, edits made VIA SkillFactory will not be applied
     * <i>however</i> if you wish to have the skill forced with your data (and be incapable of being editted without uploading a new version) you can set this to {@code 'true'}
     * This is discouraged, as we wish to be able to change values without recompiling plugins.
     *
     * @param force Whether or not to force an update of this skill
     * @return itself
     */
    SkillFactory withForceUpdate(boolean force);

    /**
     * Process the SkillFactory object to create a new skill. This method
	 * will automatically do all the required back-end work to make the skill
	 * immediately accessible and usable as per your used settings.
     *
     * @return The new Skill object, registered with your provided settings.
	 */
	Skill register();



}