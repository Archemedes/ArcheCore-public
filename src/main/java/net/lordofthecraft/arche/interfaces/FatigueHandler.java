package net.lordofthecraft.arche.interfaces;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public interface FatigueHandler {
    String NO_FATIGUE_MESSAGE = ChatColor.DARK_RED + "Warning: "
            + ChatColor.YELLOW + " You're fatigued and unable to perform your profession effectively. Either wait it out or recover at a tavern.";

    /**
	 * Add a certain amount of fatigue, respecting the upper limit
	 * @param pers Persona to handle
	 * @param add Amount to add
	 */
    void addFatigue(Persona pers, double add);

    /**
	 * Remove a certain amount of fatigue, respecting the lower limit
	 * @param pers Persona to handle
     * @param remove Amount to reduce
     */
    void reduceFatigue(Persona pers, double remove);

    /**
	 * Sets amount of fatigue (value between 0 and 100)
	 * @param pers Persona to handle
     * @param fatigue Amount to set to
     */
    void setFatigue(Persona pers, double fatigue);

    /**
	 * Sets the experience bar of the player to the fatigue value of their current persona
	 * does nothing if player has no current persona
	 * @param p Player to act on
	 */
    void showFatigueBar(Player p);

    /**
	 * Sets the experience bar of the player to the fatigue value of their current persona
	 * does nothing if player has no current persona
	 * @param pers Persona to act on
	 */
    void showFatigueBar(Persona pers);

    /**
	 * Sets the experience bar of the player to the fatigue value of their current persona
	 * does nothing if player has no current persona
	 * @param p Player to act on
	 * @param pers Persona to handle
	 */
    void showFatigueBar(Player p, Persona pers);

    /**
	 * Return fatigue value of the Persona
	 * @param pers Persona to chec
	 * @return Persona's fatigue
	 */
    double getFatigue(Persona pers);

    /**
     * Whether or not the persona has enough energy to accomplish a task
     * @param pers Persona to check
	 * @param needed amount of fatigue you want to check against
	 * @return Whether the personas fatigue is at least 'needed' away from maximum
	 */
    boolean hasEnoughEnergy(Persona pers, double needed);

    /**
	 * Racial bonuses of a skill or the Fatigue gain attribute may modify fatigue gain. 
	 * This method will recalculate based on the applicable modifiers for this persona.
	 * @param pers Persona to check the race of
	 * @param value initial fatigue value to add (negative values return unmodified)
	 * @param skill the skill to check the racial bonuses of
	 * @return the modified fatigue value
	 */
    double modifyNetFatigue(Persona pers, double value, Skill... skill);

    /**
	 * Method that condenses the most likely workflow for fatigue management while practicing a profession
	 * Checks if player has sufficient energy (outputting a message if not) to perform an action.
	 * Adds fatigue and updates the fatigue bar if the action was successfully performed
	 * @param pers Persona to handle
	 * @param add Fatigue to measure against and possibly add
	 * @param skill The skill to apply race mods forx
	 * @return Whether or not fatigue check was successful
	 */
    boolean handleFatigue(Persona pers, double add, Skill skill);

}
