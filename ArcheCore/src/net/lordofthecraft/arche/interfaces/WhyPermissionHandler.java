package net.lordofthecraft.arche.interfaces;

public interface WhyPermissionHandler {

	/**
	 * This method is used to get a list of permissions in the form of strings attached to said persona.
	 * @param target The persona that may have permissions attached to it
	 * @return An array of strings in minecraft's permission format, will be null if there are none.
	 */
	
	public String[] getPermissions(Persona target);
	
	/**
	 * Adds a specific permission to a persona, to be added when the user switches to persona and
	 * removed when they switch off of the persona. Will be removed if the persona is permakilled.
	 * @param target The persona to tac permissions onto
	 * @param permission The permission in question in the format of "[plugin].[Node].[Node]...", the usual Permission systems.
	 * @return Return if the permission adding was successful.
	 */
	
	public boolean addPermission(Persona target, String permission);
	
	/**
	 * Remove a permission from a specific persona. This code will always run, even if there is no
	 * permission node attached to the persona. It will only return false if the sql statement failed.
	 * @param target The target to remove said permission from
	 * @param permission permission The permission in question in the format of "[plugin].[Node].[Node]...", the usual Permission systems.
	 * @return If the SQL statement succeeded.
	 */
	
	public boolean removePermission(Persona target, String permission);
	
	/**
	 * Checks to see if the persona has a permission node
	 * @param target The targetted persona
	 * @param permission The permission to check for
	 * @return true if found, false if not.
	 */
	
	public boolean hasPermission(Persona target, String permission);
	
	/**
	 * Method to handle the .remove() of a persona with permissions attached to it.
	 * @param killed The persona being removed
	 * @return If the SQL statement succeeded.
	 */
	
	public boolean handlePerma(Persona killed);
	
}
