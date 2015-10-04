package net.lordofthecraft.arche.interfaces;

import java.util.UUID;

import net.lordofthecraft.arche.enums.ProfessionSlot;
import net.lordofthecraft.arche.enums.Race;
import net.lordofthecraft.arche.persona.PersonaSkin;

import org.bukkit.entity.Player;

public interface Persona {

	/**
	 * Reset a persona's visible skills (drainXp and Magic skills not effected) to 0 and allow it to be redistributed
	 * @return The amount of Xp now open to be assigned
	 */
	
	public double resetSkills();
	
	/**
	 * Assign a persona's race to the specified race as well as resets the user's racial skill in raw experience for distribution.
	 * @param r The new persona's race.
	 */
	public void racialReassign(Race r);
	
	/**
	 * A method that retrieves the Persona's 'main' skill or profession, which they can set for their Persona's RP professions
	 * @param slot The chosen slot to query
	 * @return A player's self-set 'main' skill.
	 */
	public Skill getProfession(ProfessionSlot slot);

	/**
	 * Set a Persona's choice of professions. Chosen professions can be levelled to their maximum
	 * @param slot The chosen slot to query
	 * @param profession The skill to be set as main.
	 */
	public void setProfession(ProfessionSlot slot, Skill profession);

	/**
	 * A method that retrieves the Persona's 'main' skill or profession, which they can set for RP purposes.
	 * @return A player's self-set 'main' skill.
	 */
	public Skill getMainSkill();

	/**
	 * Set a Persona's main skill or profession. Purely for aestethic purposes.
	 * @param profession The skill to be set as main.
	 */
	public void setMainSkill(Skill profession);
	
	/**
	 * Returns the session-invariant ID of the Persona. Ids are between 0 and 3.
	 * IDs are only unique for the same player 
	 * @return the immutable id of the persona
	 */
	public int getId();

	/**
	 * See if the Persona is a Player's current Persona
	 * @return Whether or not the Persona is current
	 */
	public boolean isCurrent();

	/**
	 * Sets a Personas prefix. If Prefixes are disabled, calling this will change
	 * the prefix, but not display it anywhere in the current session.
	 * @param prefix The prefix to set
	 */
	public void setPrefix(String prefix);

	/**
	 * Gets a Personas prefix.
	 * @return the Persona's current Prefix
	 */
	public String getPrefix();

	/**
	 * Does Persona have a prefix
	 * @return Whether or not Persona has a prefix
	 */
	public boolean hasPrefix();

	/**
	 * Clears a Personas prefix. If Prefixes are disabled, calling this will change
	 * the prefix, but not display it anywhere in the current session.
	 */
	public void clearPrefix();

	/**
	 * Sets whether or not this Player should gain XP.
	 * @param gainsXP Whether or not to gain XP
	 */
	public void setXPGain(boolean gainsXP);

	/**
	 * Retrieve if this Player is gaining skill experience. If false, skill experience is
	 * not gained by the player for normal actions, but can still be retrieved through teaching
	 * or skill tomes.
	 * @return If the player gains XP.
	 */
	public boolean getXPGain();

	/**
	 * Retrieve the total Playtime this Persona has seen since its creation.
	 * @return Playtime in minutes
	 */
	public int getTimePlayed();

	/**
	 * Get the amount of characters this player has entered into chat, while playing this Persona, since creation
	 * @return Amount of characters spoken.
	 */
	public int getCharactersSpoken();

	/**
	 * Retrive the name of the Player that this Persona belongs to.
	 * @return the name of the owning player.
	 */
	public String getPlayerName();

	/**
	 * Each Persona is uniquely identified with a composite key that consists of
	 * the Mojang Player UUID and a integer that refers to the Persona of the player.
	 * The PersonaKey pairs these two objects, and can be used to compare Personas and as keys in HashMaps.
	 * @return The Persona Key for this persona
	 */
	public PersonaKey getPersonaKey();
	
	
	
	/**
	 * Retrieve the Mojang-issued UUID coupled to this Persona's player
	 * @return the Player's unique id.
	 */
	public UUID getPlayerUUID();
	
	
	/**
	 * Retrieve the Player to which this Persona belongs. The Persona object does not hold any references (not even weak ones)
	 * belonging to its owning Player. The object will be found by iterating over all online players and comparing their UUID.
	 * @return The Player belonging to this character, or null if the player is not online
	 */
	public Player getPlayer();
	
	
	/**
	 * Retrieves the Persona's qualified Chat name. That is, it returns a proper concatenation of the prefix and Persona name
	 * @return The name of the Player used for chat
	 */
	public String getChatName();
	
	/**
	 * Retrieve the Roleplay name of this Persona 
	 * @return The Persona's RP name.
	 */
	public String getName();

	/**
	 * Retrieve the immutable Race of this Persona.
	 * @return Race of the Persona.
	 */
	public Race getRace();

	/**
	 * Retrieves the Human-readable Race of this Persona. This String may have been 
	 * custom-set and overrides the actual visible Race of the Persona, but not the
	 * underlying (Enum-based) race. 
	 * @return The Race of the Persona, or the Custom-set Race if applicable
	 */
	public String getRaceString();

	/**
	 * Override the Persona's visible Race with a Custom String. This does not remove
	 * the actual race of the Persona, but displays only the newly set 'apparent' race.
	 * @param race The string to be displayed as Race.
	 */
	public void setApparentRace(String race);

	/**
	 * Get the time this Persona was last renamed (as fetched by the System's currentTimeMillis on the time of the last rename)
	 * @return The moment of this Persona's last renaming.
	 */
	public long getRenamed();

	/**
	 * Set the new RP name of this Persona. This also updates the time at which this Persona was last renamed
	 * @param name The new RP name.
	 */
	public void setName(String name);

	/**
	 * Clear the biopgraphy of this Persona.
	 */
	public void clearDescription();

	/**
	 * Set the biography of this new Persona to the given String.
	 * @param description The new biography of the Persona.
	 */
	public void setDescription(String description);

	/**
	 * Adds a line to this Persona's biography, separated from the standing biography with a space.
	 * @param addendum The string to add to the Biography.
	 */
	public void addDescription(String addendum);

	/**
	 * Retrieve the entire biography of this Persona as it currently stands.
	 * @return The Persona's biography string, or null if unset.
	 */
	public String getDescription();

	/**
	 * Get the human-readable Gender of this Persona
	 * @return The Persona's gender, immutable
	 */
	public String getGender();

	/**
	 * Get the current Age of this Persona.
	 * @return the Persona's age
	 */
	public int getAge();

	/**
	 * Set the current age of this Persona.
	 * @param age the age to set.
	 */
	public void setAge(int age);

	/**
	 * Get whether or not this Persona's age is set to.
	 * automatically increase (with ingame years).
	 * @return If the Persona automatically ages
	 */
	public boolean doesAutoAge();

	/**
	 * Set whether or not this Persona should age automatically.
	 * @param autoAge If the persona should auto-age.
	 */
	public void setAutoAge(boolean autoAge);

	/**
	 * Set the current skin of this Persona.
	 * @param skin the skin to set.
	 */
	public void setSkin(PersonaSkin skin);
	
	/**
	 * Get the associated skin of this Persona.
	 * @return The Persona's skin.
	 */
	
	public PersonaSkin getSkin();
	
	/**
	 * Delete the Persona from the Plugin records.
	 * @return whether or not the removal was successful.
	 */
	
	public boolean remove();

	/**
	 * @return if the player is below the new persona timer
	 */
	public boolean isNewbie();
	
}