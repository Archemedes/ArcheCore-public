package net.lordofthecraft.arche.interfaces;

import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.chat.BaseComponent;

public interface CommandHandle {

	/**
	 * @return whoever issued the command, which may not be the Player/Persona target
	 */
	public CommandSender getSender();
	
	
	/**
	 * Send a formatted message to the command issuer
	 * @param message The message in String.format / printf compliant style
	 * @param format the formatting parameters
	 */
	public void msg(String message, Object... format);
	
	/**
	 * Send a Json-formatted message to the command issuer (Json features only work on players)
	 * @param message The message in a bungee BaseComponent format
	 */
	public void msg(BaseComponent message);
	
	/**
	 * Send something to the command issuer
	 * @param message an unformatted string message
	 */
	public void msgRaw(String message);
	
	/**
	 * Terminate the command sequence execution immediately in a way that the command engine understands
	 * @param err an erorr message that will be formatted and provided to the command sender
	 */
	public void error(String err);
	
	/**
	 * Shorthand method to simplify the command flow. Require something is true or terminate with an error message otherwise.
	 * @param condition something that you require to be true at this point in the command sugar
	 * @param error the error message the commandSender will see if the condition fails
	 */
	public void validate(boolean condition, String error);
}
