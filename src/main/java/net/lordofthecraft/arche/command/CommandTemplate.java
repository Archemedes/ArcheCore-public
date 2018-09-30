package net.lordofthecraft.arche.command;

import org.bukkit.command.CommandSender;

import lombok.AccessLevel;
import lombok.Setter;
import net.lordofthecraft.arche.interfaces.CommandHandle;
import net.md_5.bungee.api.chat.BaseComponent;

public abstract class CommandTemplate implements CommandHandle {
	@Setter(AccessLevel.PACKAGE) RanCommand ranCommand;
	
	@Override
	public CommandSender getSender() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void msg(String message, Object... format) {
		// TODO Auto-generated method stub

	}

	@Override
	public void msg(BaseComponent message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void msgRaw(String message) {
		// TODO Auto-generated method stub

	}

	@Override
	public void error(String err) {
		// TODO Auto-generated method stub

	}

	@Override
	public void validate(boolean condition, String error) {
		// TODO Auto-generated method stub

	}

}
