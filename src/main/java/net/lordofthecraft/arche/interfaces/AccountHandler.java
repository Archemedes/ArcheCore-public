package net.lordofthecraft.arche.interfaces;

import java.util.UUID;

import net.lordofthecraft.arche.account.Waiter;

public interface AccountHandler {

	Account getAccount(UUID u);
	
	Account getAccount(int id);
	
	boolean isLoaded(UUID u);
	
	Waiter<Account> loadAccount(UUID u);

	}
