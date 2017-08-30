package net.lordofthecraft.arche.interfaces;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.persona.PersonaInventory;
import net.lordofthecraft.arche.persona.PersonaSkills;

public interface Persona {

	/**
	 * Take money from a Persona. Negative amounts possible, but consider using {@link #deposit(double)}  instead.
	 * @param amount The amount of money to modify the Persona account by
	 *
	 * @return Amount of minas persona possess after the transaction
	 * @deprecated Use {@link #withdraw(double, Transaction)}
	 */
	@Deprecated()
	double withdraw(double amount);

	double withdraw(double amount, Transaction cause);

	/**
	 * Give money to a Persona. Negative amounts possible, but consider using {@link #withdraw(double)} instead.
	 * @param amount The amount of money to modify the Persona account by
	 *
	 * @return Amount of minas persona possess after the transaction
	 * @deprecated Use {@link #deposit(double, Transaction) }
	 */
	@Deprecated()
	double deposit(double amount);

	double deposit(double amount, Transaction cause);

	/**
	 * The PersonaSkills objects hold the fields and methods related to a particular persona's skills
	 * @return The PersonaSkills object
	 */
	PersonaSkills getPersonaSkills();
	
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
	 * Returns the session-invariant ID of the Persona. Ids are between 0 and 3.
	 * IDs are only unique for the same player 
	 * @return the immutable id of the persona
	 */
	int getId();

	/**
	 * See if the Persona is a Player's current Persona
	 * @return Whether or not the Persona is current
	 */
	boolean isCurrent();

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

	public double getFatigue();
	
	public void setFatigue(double fatigue);
	
	
	/**
	 * Clears a Personas prefix. If Prefixes are disabled, calling this will change
	 * the prefix, but not display it anywhere in the current session.
	 */
	void clearPrefix();

	/**
	 * Retrieve the total Playtime this Persona has seen since its creation.
	 * @return Playtime in minutes
	 */
	int getTimePlayed();

	/**
	 * Get the amount of characters this player has entered into chat, while playing this Persona, since creation
	 * @return Amount of characters spoken.
	 */
	int getCharactersSpoken();

	/**
	 * Retrive the name of the Player that this Persona belongs to.
	 * @return the name of the owning player.
	 */
	String getPlayerName();

	/**
	 * Each Persona is uniquely identified with a composite key that consists of
	 * the Mojang Player UUID and a integer that refers to the Persona of the player.
	 * The PersonaKey pairs these two objects, and can be used to compare Personas and as keys in HashMaps.
	 * @return The Persona Key for this persona
	 */
	PersonaKey getPersonaKey();



	/**
	 * Retrieve the Mojang-issued UUID coupled to this Persona's player
	 * @return the Player's unique id.
	 */
	UUID getPlayerUUID();


	/**
	 * Retrieve the Player to which this Persona belongs. The Persona object does not hold any references (not even weak ones)
	 * belonging to its owning Player. The object will be found by iterating over all online players and comparing their UUID.
	 * @return The Player belonging to this character, or null if the player is not online
	 */
	Player getPlayer();


	/**
	 * Retrieves the Persona's qualified Chat name. That is, it returns a proper concatenation of the prefix and Persona name
	 * @return The name of the Player used for chat
	 */
	String getChatName();

	/**
	 * Retrieve the Roleplay name of this Persona 
	 * @return The Persona's RP name.
	 */
	String getName();

	/**
	 * Set the new RP name of this Persona. This also updates the time at which this Persona was last renamed
	 * @param name The new RP name.
	 */
	void setName(String name);

	/**
	 * Retrieve the immutable Race of this Persona.
	 * @return Race of the Persona.
	 */
	Race getRace();

	/**
	 * Retrieves the Human-readable Race of this Persona. This String may have been
	 * custom-set and overrides the actual visible Race of the Persona, but not the
	 * underlying (Enum-based) race.
	 * @return The Race of the Persona, or the Custom-set Race if applicable
	 */
	String getRaceString();

	/**
	 * Override the Persona's visible Race with a Custom String. This does not remove
	 * the actual race of the Persona, but displays only the newly set 'apparent' race.
	 * @param race The string to be displayed as Race.
	 */
	void setApparentRace(String race);

	/**
	 * Get the time this Persona was last renamed (as fetched by the System's currentTimeMillis on the time of the last rename)
	 * @return The moment of this Persona's last renaming.
	 */
	long getRenamed();

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
	 * Get the human-readable Gender of this Persona
	 * @return The Persona's gender
	 */
	String getGender();

	/**
	 * Assign a persona's gender to the specified gender.
	 * @param r The persona's new gender.
	 */

	void setGender(String gender);

	/**
	 * Delete the Persona from the Plugin records.
	 * @return whether or not the removal was successful.
	 */

	boolean remove();

	/**
	 * @return if the player is below the new persona timer
	 */
	boolean isNewbie();

	/**
	 * @return The PersonaInventory for this persona
	 */
	PersonaInventory getPInv();

	/** Get the current head icon of this persona.
	 * @return The Persona's icon.
	 */

	/**
	 * @return the inventory of this persona as an Inventory object
	 */
	Inventory getInventory();

	/**
	 * @return the creation time of this persona in milliseconds
	 */
	long getCreationTime();



	/**
	 * @return the total playtime of this persona(all maps added)
	 */
	int getTotalPlaytime();
}