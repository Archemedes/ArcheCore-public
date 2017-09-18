package net.lordofthecraft.arche;

import net.lordofthecraft.arche.interfaces.Transaction;

public class ArcheCoreTransaction implements Transaction {
	private final String cause;
	
	public ArcheCoreTransaction(String cause) {
		this.cause = cause;
	}
	
	@Override
	public String getCause() {
		return cause;
	}

	@Override
	public String getRegisteringPluginName() {
		return ArcheCore.getPlugin().getName();
	}

    @Override
    public String toString() {
        return "ArcheCoreTransaction{" +
                "cause='" + cause + '\'' +
                '}';
    }
}
