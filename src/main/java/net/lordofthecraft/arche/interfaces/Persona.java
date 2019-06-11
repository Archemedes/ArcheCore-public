package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.enums.PersonaType;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.persona.PersonaAttributes;
import net.lordofthecraft.arche.persona.PersonaSkills;
import net.lordofthecraft.arche.skin.ArcheSkin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.sql.Timestamp;
import java.util.Set;

public interface Persona extends OfflinePersona {

	/**
	 * Take money from a Persona. Negative amounts possible, but consider using {@link #deposit(double, Transaction)} instead.
	 *
	 * @param amount The amount of money to modify the Persona account by
	 * @param cause  The reason this money is being removed
	 * @return Amount of minas persona possess after the transaction
	 */
	double withdraw(double amount, Transaction cause);

	/**
	 * Give money to a Persona. Negative amounts possible, but consider using {@link #withdraw(double, Transaction)} instead.
	 * @param amount The amount of money to modify the Persona account by
	 * @param cause The reason this money is being added
	 * @return Amount of minas persona possess after the transaction
	 */
	double deposit(double amount, Transaction cause);

	/**
	 * The PersonaSkills objects hold the fields and methods related to a particular persona's skills
	 * @return The PersonaSkills object
	 */
	PersonaSkills skills();

	/**
	 * The PersonaAttributes objects hold the fields and methods related to a particular persona's attributes
	 * @return The personaAttributes object
	 */
	PersonaAttributes attributes();

	/**
	 * A method that retrieves the Persona's 'main' skill or profession, which they can set for RP purposes.
	 * @return A player's self-set 'main' skill.
	 */
	Skill getMainSkill();

	/**
	 * Set a Persona's main skill or profession. Purely for aesthetic purposes.
	 * @param profession The skill to be set as main.
	 */
	void setMainSkill(Skill profession);

	/**
	 * Set a Persona's year of birth
	 */
	void setDateOfBirth(int birthdate);

	/**
	 * Assign a persona's gender to the specified gender.
	 *
	 * @param gender The persona's new gender.
	 */
	void setGender(String gender);

	/**
	 * Set the new RP name of this Persona. This also updates the time at which this Persona was last renamed
	 *
	 * @param name The new RP name.
	 */
	void setName(String name);

	/**
	 * @return All previous names this Persona has had
	 */
	Set<String> getPastNames();

	/**
	 * Set the underlying type of this persona
	 *
	 * @param type The type of persona it should be.
	 */
	void setPersonaType(PersonaType type);

	/**
	 * Gets a Personas prefix.
	 * @return the Persona's current Prefix
	 */
	String getPrefix();

	/**
	 * Sets a Personas prefix. If Prefixes are disabled, calling this will change
	 * the prefix, but not display it anywhere in the current session.
	 * @param prefix The prefix to set
	 */
	void setPrefix(String prefix);

	/**
	 * Does Persona have a prefix
	 * @return Whether or not Persona has a prefix
	 */
	boolean hasPrefix();

	/**
	 * Gets the current level of Fatigue this persona has, between 0 and 100ish
	 *
	 * @return The current level of fatigue
	 * @see net.lordofthecraft.arche.ArcheFatigueHandler
	 */
	double getFatigue();

	/**
	 * Sets the current level of Fatigue for this persona and performs a quick {@link net.lordofthecraft.arche.save.rows.persona.update.PersonaUpdateRow}
	 *
	 * @param fatigue The new level of fatigue
	 */
	void setFatigue(double fatigue);

	/**
	 * Clears a Personas prefix. If Prefixes are disabled, calling this will change
	 * the prefix, but not display it anywhere in the current session.
	 */
	void clearPrefix();

	/**
	 * @return Last time this Persona was switched to or from
	 */
	long getLastSeen();
	
  /**
   * Retrieve the total Playtime this Persona has seen since the last reset
   *
   * @return Playtime in minutes
   */
  int getTimePlayed();
	
	/**
	 * Get the amount of characters this player has entered into chat, while playing this Persona, since creation
	 * @return Amount of characters spoken.
	 */
	int getCharactersSpoken();

	/**
	 * Retrieve the Player to which this Persona belongs, but only if the Player is online.
	 * @return The Player belonging to this character, or null if the player is not online
	 */
	@Override
	Player getPlayer();

	/**
	 * @return Their Account
	 */
	Account getAccount();
	
	/**
	 * Retrieves the Persona's qualified Chat name. That is, it returns a proper concatenation of the prefix and Persona name
	 * @return The name of the Player used for chat
	 */
	String getChatName();

	/**
	 * Get the time this Persona was last renamed (as fetched by the System's currentTimeMillis on the time of the last rename)
	 * @return The moment of this Persona's last renaming.
	 */
	Timestamp getRenamed();

	/**
	 * Clear the biopgraphy of this Persona.
	 */
	void clearDescription();

	/**
	 * Adds a line to this Persona's biography, separated from the standing biography with a space.
	 * @param addendum The string to add to the Biography.
	 */
	void addDescription(String addendum);

	/**
	 * Retrieve the entire biography of this Persona as it currently stands.
	 * @return The Persona's biography string, or null if unset.
	 */
	String getDescription();

	/**
	 * Set the biography of this new Persona to the given String.
	 * @param description The new biography of the Persona.
	 */
	void setDescription(String description);

	/**
	 * @return The EnderChest inventory for this persona
	 */
	Inventory getEnderChest();

	/**
	 * @return the inventory of this persona as an Inventory object
	 */
	Inventory getInventory();

	/**
	 * @return if the player is below the new persona timer
	 */
	boolean isNewbie();

	boolean shouldProtectNewbie();
	
	void setNewbie(boolean newbie);
	
	/**
	 * Sets the skin of this persona to use
	 *
	 * @param skin The skin this persona will be using
	 */
	void setSkin(ArcheSkin skin);

	/**
	 * Removes the current {@link net.lordofthecraft.arche.skin.ArcheSkin} from the persona, regardless of if one is present or not. Extra dead.
	 */
	void removeSkin();

	/**
	 * Get the current skin on this persona
	 *
	 * @return The skin of this persona. Will be null if there is no skin.
	 */
	ArcheSkin getSkin();

	/**
	 * Checks to see if this persona has an assigned skin
	 *
	 * @return Returns whether or not this persona has an {@link net.lordofthecraft.arche.skin.ArcheSkin}
	 */
	boolean hasSkin();

	/**
	 * @return the total playtime of this persona(all maps added)
	 */
	int getTotalPlaytime();

	/**
	 * Assign a persona's race to the specified race.
	 * @param race The persona's new race
	 */
	void setRace(Race race);

	/**
	 * Override the Persona's visible Race with a Custom String. This does not remove
	 * the actual race of the Persona, but displays only the newly set 'apparent' race.
	 * @param race The string to be displayed as Race.
	 */
	void setApparentRace(String race);
}