package net.lordofthecraft.arche;

import org.bukkit.plugin.Plugin;

import net.lordofthecraft.arche.interfaces.Transaction;

public class SimpleTransaction implements Transaction {
	private final String cause;
	private final TransactionType type;
	
	public SimpleTransaction(String reason, TransactionType type) {
		this.cause = reason;
		this.type = type;
	}
	
	
	@Override
	public String getCause() {
		return cause;
	}

	@Override
	public TransactionType getType() {
		return type;
	}

	@Override
	public Plugin getRegisteringPlugin() {
		return ArcheCore.getPlugin();
	}

	@Override
	public String getRegisteringPluginName() {
		return ArcheCore.getPlugin().getName();
	}

}
