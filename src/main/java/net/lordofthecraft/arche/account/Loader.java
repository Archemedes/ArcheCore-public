package net.lordofthecraft.arche.account;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;

import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.Persona;

public class Loader {
	private final Set<UUID> confirmed = new HashSet<>();
	
	private final List<Future<Persona>> personas = new ArrayList<>();
	private final List<Future<Account>> accounts = new ArrayList<>();
	

	public synchronized void check() {
		
	}
	
	public synchronized void deliver() {
		
	}
	
}
