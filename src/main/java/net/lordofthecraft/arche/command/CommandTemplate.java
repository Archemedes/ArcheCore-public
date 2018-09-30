package net.lordofthecraft.arche.command;

import org.bukkit.command.CommandSender;

import lombok.AccessLevel;
import lombok.Setter;
import net.lordofthecraft.arche.interfaces.CommandHandle;
import net.md_5.bungee.api.chat.BaseComponent;

//Decorator class around RanCommand for the purposes of annotated command handling
public abstract class CommandTemplate implements CommandHandle {
	@Setter(AccessLevel.PACKAGE) RanCommand ranCommand;
	
	@Override
	public CommandSender getSender() {
		return ranCommand.getSender();
	}

	@Override
	public void msg(String message, Object... format) {
		ranCommand.msg(message, format);
	}

	@Override
	public void msg(BaseComponent message) {
		ranCommand.msg(message);
	}

	@Override
	public void msgRaw(String message) {
		ranCommand.msgRaw(message);
	}

	@Override
	public void error(String err) {
		ranCommand.error(err);
	}

	@Override
	public void validate(boolean condition, String error) {
		ranCommand.validate(condition, error);
	}

}
