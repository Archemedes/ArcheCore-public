package net.lordofthecraft.arche.interfaces;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public interface FatigueHandler {
	public static final String NO_FATIGUE_MESSAGE = ChatColor.DARK_RED + "Warning: " 
			+ ChatColor.YELLOW + " You're fatigued and unable to perform your profession effectively. Either wait it out or recover at a tavern.";
	public static final double MAXIMUM_FATIGUE = 100.0;
	
	/**
	 * Add a certain amount of fatigue, respecting the upper limit
	 * @param pers Persona to handle
	 * @param add Amount to add
	 */
	public void addFatigue(Persona pers, double add);
	
	/**
	 * Remove a certain amount of fatigue, respecting the lower limit
	 * @param pers Persona to handle
	 * @param add Amount to reduce
	 */
	public void reduceFatigue(Persona pers, double remove);
	
	/**
	 * Sets amount of fatigue (value between 0 and 100)
	 * @param pers Persona to handle
	 * @param add Amount to set to
	 */
	public void setFatigue(Persona pers, double fatigue);
	
	/**
	 * Sets the experience bar of the player to the fatigue value of their current persona
	 * does nothing if player has no current persona
	 * @param p Player to act on
	 */
	public void showFatigueBar(Player p);
	
	/**
	 * Sets the experience bar of the player to the fatigue value of their current persona
	 * does nothing if player has no current persona
	 * @param p Player to act on
	 * @param pers Persona to handle
	 */
	public void showFatigueBar(Player p, Persona pers);
	
	/**
	 * Return fatigue value of the Persona
	 * @param pers Persona to chec
	 * @return Persona's fatigue
	 */
	public double getFatigue(Persona pers);
	
	/**
	 * Whether
	 * @param pers Persona to check
	 * @param needed amount of fatigue you want to check against
	 * @return Whether the personas fatigue is at least 'needed' away from maximum
	 */
	public boolean hasEnoughEnergy(Persona pers, double needed);
	
	/**
	 * Method that condenses the most likely workflow for fatigue management while practicing a profession
	 * Checks if player has sufficient energy (outputting a message if not) to perform an action.
	 * Adds fatigue and updates the fatigue bar if the action was successfully performed
	 * @param pers Persona to handle
	 * @param add Fatigue to measure against and possibly add
	 * @return Whether or not fatigue check was successful
	 */
	public boolean handleFatigue(Persona pers, double add);
	
}
