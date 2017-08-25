package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.enums.ChatBoxAction;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;

public interface ChatMessage {

	/**
	 *  Adds a raw line of text, faster but produces odd results if containing Minecraft color codes.
	 * @param line Raw line of text to add
	 * @return itself
	 */
	ChatMessage addRawLine(String line);
	
	/**
	 *  Adds a line of text, respecting the legacy Minecraft formatting and color codes
	 * @param line Line of text to add
	 * @return itself
	 */
	ChatMessage addLine(String line);

	/**
	 * Add a translatable line of text, respecting MC's internal translation strings
	 * @param translate Internal translation string to add
	 * @param with Additional text used within translation
	 * @return itself
	 */
	ChatMessage addTranslation(String translate, String... with);

	/**
	 * Select one of the TextComponents that make up this message
	 * @param i Index of the component
	 * @return a new ChatMessage wrapped around a single part 
	 */
	ChatMessage select(int i);

	/**
	 * Get the amount of top-level BaseComponents that make up this message 
	 * @return count of components
	 */
	int size();

	/**
	 * Apply Minecraft's color formatting options to the current segment of the message
	 * @param color The ChatColor to apply
	 * @return itself
	 */
	ChatMessage applyChatColor(org.bukkit.ChatColor color);

	/**
	 * Removes a line from the ChatMessage
	 * @return false Index out of bounds
	 * @return true Chat message was removed
	 */
	boolean removeLine(int i);

	/**
	 * How many parts are in the ChatMessage
	 * @return Amount of parts
	 */
	int size(int i);


	/**
	 * Make current section bold
	 * @return itself
	 */
	ChatMessage setBold();

	/**
	 * Underline current section
	 * @return itself
	 */
	ChatMessage setUnderlined();

	/**
	 * Italicize current section
	 * @return itself
	 */
	ChatMessage setItalic();

	/**
	 * Strikethrough current section
	 * @return itself
	 */
	ChatMessage setStrikethrough();

	/**
	 * Obfuscate current section ('MAGIC' chat color)
	 * @return itself
	 */
	ChatMessage setObfuscated();

	/**
	 * Apply to the current section an event to be executed on Click. Note that only the following are valid for a ClickEvent:
	 * {@link net.lordofthecraft.arche.enums.ChatBoxAction#RUN_COMMAND RUN_COMMAND}
	 * {@link net.lordofthecraft.arche.enums.ChatBoxAction#SUGGEST_COMMAND SUGGEST_COMMAND}
	 * {@link net.lordofthecraft.arche.enums.ChatBoxAction#OPEN_URL OPEN_URL}
	 * {@link net.lordofthecraft.arche.enums.ChatBoxAction#OPEN_FILE OPEN_FILE}
	 * @param action The action to be taken on clicking
	 * @param value String value to use, which functions based on the chosen action
	 * @return itself
	 */
	ChatMessage setClickEvent(ChatBoxAction action, String value);

	/**
	 * Apply a formatted ClickEvent to current section of the ChatMessage
	 * @param event ClickEvent to use
	 * @return itself
	 */
	ChatMessage setClickEvent(ClickEvent event);

	/**
	 * Apply to the current section an event to be executed on Click. Note that only the following are valid for a ClickEvent:
	 * {@link net.lordofthecraft.arche.enums.ChatBoxAction#SHOW_ACHIEVEMENT SHOW_ACHIEVEMENT}
	 * {@link net.lordofthecraft.arche.enums.ChatBoxAction#SHOW_TEXT SHOW_TEXT}
	 * {@link net.lordofthecraft.arche.enums.ChatBoxAction#SHOW_ITEM SHOW_ITEM}
	 * @param action The action to be taken on hovering
	 * @param value String value to use, which functions based on the chosen action
	 * @return itself
	 */
	ChatMessage setHoverEvent(ChatBoxAction action, String value);

	/**
	 * Apply a formatted HoverEvent to current section of the ChatMessage
	 * @param event HoverEvent to use
	 * @return itself
	 */
	ChatMessage setHoverEvent(HoverEvent event);

	/**
	 * Send formatted message out to Player
	 * @param p Player to send to
	 */
	void sendTo(Player p);

	/**
	 * Readable text format of the ChatMessage
	 * @return ChatMessage text body
	 */
	String toText();



}