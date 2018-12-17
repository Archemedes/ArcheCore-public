package net.lordofthecraft.arche.account;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

import lombok.AllArgsConstructor;
import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;

@AllArgsConstructor
public class Loader {
	private final ArcheAccountHandler aHandler;
	private final ArchePersonaHandler pHandler;
	
	private final Set<UUID> confirmed = new HashSet<>();
	
	private final List<Waiter<Account>> accounts = new ArrayList<>();
	private final List<Waiter<Persona>> personas = new ArrayList<>();

	public synchronized boolean isLoaded(UUID u) {
		return confirmed.contains(u);
	}
	
	public synchronized Waiter<Account> check(UUID u) {
		if(isLoaded(u)) {
			Account acc = aHandler.getAccount(u);
			return Waiter.wrap(acc);
		} else {
			var r = new Waiter<Account>(u);
			accounts.add(r);
			return r;
		}
	}
	
	public synchronized Waiter<Persona> check(OfflinePersona p){
		var u = p.getPlayerUUID();
		if(isLoaded(u)) {
			Persona op = pHandler.getPersona(u,p.getSlot());
			return Waiter.wrap(op);
		} else {
			var r = new Waiter<Persona>(u, p.getPersonaId());
			personas.add(r);
			return r;
		}
	}
	
	public void deliver(AccountBlob blob) {
		if(!Bukkit.isPrimaryThread()) throw new ConcurrentModificationException("Please deliver personas and accounts from main");
		
		List<Waiter<Account>> ats = new ArrayList<>();
		Map<Waiter<Persona>, ArchePersona> pts = new HashMap<>();
		
		synchronized(this) {
			UUID anyUUID = blob.getAccount().getUUIDs().stream().findAny().get();
			if(confirmed.contains(anyUUID)) {
				CoreLog.warning("Interleaved loading detected when trying to deliver Account for " + anyUUID);
				return; //Our work has been done already
			}
			 
			aHandler.implement(blob.getAccount());
			pHandler.getPersonaStore().implement(blob.getAccount().getUUIDs(), blob.getPersonas());
			blob.getAccount().getUUIDs().forEach(u -> confirmed.add(u));
			
			//Extract the Waiter objects we can solve
			//These lists must be accessed sync only
			var it1 = accounts.iterator();
			while(it1.hasNext()) {
				var acc = it1.next();
				blob.getAccount().getUUIDs().stream()
				.filter(acc::isUUID)
				.findAny().ifPresent(p->{
					it1.remove();
					ats.add(acc);
				});
			}
			
			var it2 = personas.iterator();
			while(it2.hasNext()) {
				var prs = it2.next();
				blob.getPersonas().stream()
				.filter(p->prs.isId(p.getPersonaId()))
				.findAny().ifPresent(p->{
					it2.remove();
					pts.put(prs,p);
				});
			}
		}
		
		//Fulfil the waiter objects
		ats.forEach(acc->acc.fulfil(blob.getAccount()));
		pts.forEach((prs,p)->prs.fulfil(p));
	}
	
	void initialize(UUID player) {
		AccountBlob blob = loadFromDisk(player);
		if(blob != null) sync(()->deliver(blob));
	}
	
	private AccountBlob loadFromDisk(UUID u) {
		ArcheAccount acc = aHandler.fetchAccount(u);
		
		List<ArchePersona> prs = new ArrayList<>();
		for(UUID u2 : acc.getUUIDs()) {
			var personas = pHandler.getPersonaStore().loadPersonas(u2);
			personas.forEach(prs::add);
		}
		
		AccountBlob blob = new AccountBlob(acc, prs);
		aHandler.initTimes(blob);
		
		return blob;
	}
	
	private void sync(Runnable r) {
		if(Bukkit.isPrimaryThread()) r.run();
		else Bukkit.getScheduler().scheduleSyncDelayedTask(ArcheCore.getPlugin(), r);
	}
	
}
