package net.lordofthecraft.arche.interfaces;

import java.util.UUID;

import org.bukkit.entity.Player;

import net.lordofthecraft.arche.account.Waiter;

public interface AccountHandler {

	Account getAccount(Player p);
	
	Account getAccount(UUID u);
	
	Account getAccount(int id);
	
	boolean isLoaded(UUID u);
	
	Waiter<Account> loadAccount(UUID u);

	}
