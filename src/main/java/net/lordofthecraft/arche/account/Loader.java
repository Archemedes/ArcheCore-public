package net.lordofthecraft.arche.account;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
	private final ArcheCore plugin = ArcheCore.getPlugin();
	private final ArcheAccountHandler aHandler;
	private final ArchePersonaHandler pHandler;
	
	//These must only be done from synchronized blocks
	private final Set<UUID> confirmed = new HashSet<>();
	private final List<Waiter<Account>> accounts = new ArrayList<>();
	private final List<Waiter<Persona>> personas = new ArrayList<>();
	//End synchronized warning
	
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
			var uuids = blob.getAccount().getUUIDs();
			if(uuids.stream().anyMatch(confirmed::contains)) {
				int id = blob.getAccount().getId();
				if(confirmed.containsAll(uuids)) {
					CoreLog.warning("Interleaved loading detected when trying to deliver Account "+id+" for " + uuids.stream().findAny().get());
				} else {
					CoreLog.severe("Illegal/Corrupt loading state for account "+id+": Some but not all UUIDs were confirmed loaded.");
					for(var uuid : uuids) CoreLog.severe("UUID: " + uuid + ". Confirmed: " + confirmed.contains(uuid));
				}
			}
			UUID anyUUID = blob.getAccount().getUUIDs().stream().findAny().get();
			if(confirmed.contains(anyUUID)) {
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
		if(!isLoaded(player)) {
			AccountBlob blob = loadFromDisk(player);
			if(blob != null) { //Would this work?
				if(Bukkit.isPrimaryThread()) {
					deliver(blob);
				} else { //Definitely not main thread
					
					Callable<?> ccc =  Executors.callable(()->deliver(blob));
					var future = Bukkit.getScheduler().callSyncMethod(plugin, ccc);
					try{ // This stalls main thread, notice primary thread check
						future.get(5, TimeUnit.SECONDS); //This will fuck up if main thread stalls
					} catch(InterruptedException | ExecutionException | TimeoutException e) {
						//Delivery failed likely means main thread will load account a second time
						//This is fine in most cases, just slow. Issues when player is entirely new (new account)
						CoreLog.severe("Async (PlayerPreLogin) thread exhausted waiting for main thread to deliver persona blob");
						CoreLog.severe("Error received was: " + e.getClass().getName());
					}
				}
					
			}
		} else {
			CoreLog.debug("User logged in that was already loaded: " + player);
		}
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
	
}
