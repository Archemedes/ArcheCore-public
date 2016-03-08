package net.lordofthecraft.arche.interfaces;

import net.lordofthecraft.arche.enums.ChatBoxAction;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

import org.bukkit.entity.Player;

public interface ChatMessage {

	/**
	 *  Adds a line of text, respecting the legacy Minecraft formatting and color codes
	 * @param line Line of text to add
	 * @return itself
	 */
	public ChatMessage addLine(String line);
	
	/**
	 * Add a translatable line of text, respecting MC's internal translation strings
	 * @param translate Internal translation string to add
	 * @param with Additional text used within translation
	 * @return itself
	 */
	public ChatMessage addTranslation(String translate, String... with);

	/**
	 * Select one of the TextComponents that make up this message
	 * @param i Index of the component
	 * @return a new ChatMessage wrapped around a single part 
	 */
	public ChatMessage select(int i);

	/**
	 * Get the amount of top-level BaseComponents that make up this message 
	 * @return count of components
	 */
	public int size();

	/**
	 * Apply Minecraft's color formatting options to the current segment of the message
	 * @param color The ChatColor to apply
	 * @return itself
	 */
	public ChatMessage applyChatColor(org.bukkit.ChatColor color);

	/**
	 * Make current section bold
	 * @return itself
	 */
	public ChatMessage setBold();

	/**
	 * Underline current section
	 * @return itself
	 */
	public ChatMessage setUnderlined();

	/**
	 * Italicize current section
	 * @return itself
	 */
	public ChatMessage setItalic();

	/**
	 * Strikethrough current section
	 * @return itself
	 */
	public ChatMessage setStrikethrough();

	/**
	 * Obfuscate current section ('MAGIC' chat color)
	 * @return itself
	 */
	public ChatMessage setObfuscated();

	/**
	 * Apply to the current section an event to be executed on Click. Note that only the following are valid for a ClickEvent:
	 * {@link package net.lordofthecraft.arche.enums.ChatBoxAction#RUN_COMMAND}
	 * {@link package net.lordofthecraft.arche.enums.ChatBoxAction#SUGGEST_COMMAND}
	 * {@link package net.lordofthecraft.arche.enums.ChatBoxAction#OPEN_URL}
	 * {@link package net.lordofthecraft.arche.enums.ChatBoxAction#OPEN_FILE}
	 * @param action The action to be taken on clicking
	 * @param value String value to use, which functions based on the chosen action
	 * @return itself
	 */
	public ChatMessage setClickEvent(ChatBoxAction action, String value);

	/**
	 * Apply a formatted ClickEvent to current section of the ChatMessage
	 * @param event ClickEvent to use
	 * @return itself
	 */
	public ChatMessage setClickEvent(ClickEvent event);

	/**
	 * Apply to the current section an event to be executed on Click. Note that only the following are valid for a ClickEvent:
	 * {@link package net.lordofthecraft.arche.enums.ChatBoxAction#SHOW_ACHIEVEMENT}
	 * {@link package net.lordofthecraft.arche.enums.ChatBoxAction#SHOW_TEXT}
	 * {@link package net.lordofthecraft.arche.enums.ChatBoxAction#SHOW_ITEM}
	 * @param action The action to be taken on hovering
	 * @param value String value to use, which functions based on the chosen action
	 * @return itself
	 */
	public ChatMessage setHoverEvent(ChatBoxAction action, String value);

	/**
	 * Apply a formatted HoverEvent to current section of the ChatMessage
	 * @param event HoverEvent to use
	 * @return itself
	 */
	public ChatMessage setHoverEvent(HoverEvent event);

	/**
	 * Send formatted message out to Player
	 * @param p Player to send to
	 */
	public void sendTo(Player p);

	/**
	 * Readable text format of the ChatMessage
	 * @return ChatMessage text body
	 */
	public String toText();

	

}