package net.lordofthecraft.arche.account;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import lombok.var;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;

public class Loader {
	private final Set<UUID> confirmed = new HashSet<>();
	
	private final List<Waiter<Account>> accounts = new ArrayList<>();
	private final List<Waiter<Persona>> personas = new ArrayList<>();

	public synchronized Waiter<Account> check(UUID u) {
		
		return null;
	}
	
	public synchronized Waiter<Persona> check(OfflinePersona pers){
		
		return null;
	}
	
	public synchronized void deliver(AccountBlob blob) {
		for(var acc : accounts) {
			blob.getAccount().getUUIDs().stream()
				.filter(acc::isUUID)
				.findAny().ifPresent($->acc.fulfil(blob.getAccount()));
		}
		
		for(var prs : personas) {
			blob.getPersonas().stream()
				.filter(p->prs.isId(p.getPersonaId()))
				.findAny().ifPresent(p->prs.fulfil(p));
		}
		
		//TODO add to stores
		blob.getAccount().getUUIDs().forEach(u -> confirmed.add(u));
	}
	
	AccountBlob loadFromDisk(UUID u, boolean createIfAbsent) {
		
		ArcheAccount acc = ArcheAccountHandler.getInstance().fetchAccount(u, createIfAbsent);
		List<ArchePersona> prs = new ArrayList<>();
		for(UUID u2 : acc.getUUIDs()) {
			ArchePersonaHandler.getInstance().getPersonaStore().loadPersonas(u2);
		}
		
		return new AccountBlob(acc, prs);
	}
	
}
