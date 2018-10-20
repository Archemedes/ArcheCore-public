package net.lordofthecraft.arche.interfaces;

import java.util.Collection;
import java.util.Map;

import net.lordofthecraft.arche.persona.TagAttachment;

public interface Tags<T> {
	
	/**
	 * @return the object which the data Tags are associated with
	 */
	T getHolder();

	/**
	 * @param key the tag key
	 * @return The associated tag value as a string, or null if not found
	 */
	String getValue(String key);

	/**
	 * @param key the tag key
	 * @param type The type of object to attempt to cast/deserialize to
	 * @return The associated tag value as from a gson deserialization attempt, or null if key not found.
	 */
	Object getValue(String key, Class<?> type);
	
	/**
	 * @param key the tag key
	 * @return the related TagAttachment, if found
	 */
	TagAttachment getTag(String key);

	/**
	 * Fetches all the TagAttachments linked to this (Offline)Persona within the scope.
	 *
	 * @param key the tag key
	 * @return All TagAttachments linked to this persona
	 */
	Collection<TagAttachment> getTags();

	/**
	 * Fetches all the TagAttachments linked to this (Offline)Persona within the scope.
	 *
	 * @param key the tag key
	 * @return A map with all TagAttachments linked to this persona
	 */
	Map<String, TagAttachment> getTagMap();

	/**
	 * @param key the tag key
	 * @return If the TagAttachment exists for this (Offline)Persona
	 */
	boolean hasTag(String key);

	/**
	 * see {@link #giveTag(TagAttachment)}
	 */
	void giveTag(String name, Object value);

	/**
	 * see {@link #giveTag(TagAttachment)}
	 */
	void giveTag(String name, Object value, boolean offline);

	/**
	 * Give a Tag to be attached and saved with the Persona.
	 *
	 * @param tag The tag you wish to add
	 * @throws IllegalArgumentException if trying to use Tag for online-reading on offline persona
	 */
	void giveTag(TagAttachment tag);

	/**
	 * Remove a tag related to the Persona.
	 *
	 * @param tag The tag you wish to remove
	 * @return if the tag existed
	 */
	boolean removeTag(String key);
	
}
