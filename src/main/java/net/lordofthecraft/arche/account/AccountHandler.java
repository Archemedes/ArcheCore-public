package net.lordofthecraft.arche.account;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.Tags;
import net.lordofthecraft.arche.interfaces.Toon;

public class AccountHandler {
	private final Map<UUID, ArcheAccount> accounts = new HashMap<>();
	private final Map<Integer, ArcheAccount> accountsById = new HashMap<>();
	private final Map<UUID, ArcheToon> toons = new HashMap<>();
	
	private final Map<Integer, Tags<Account>> accountTags = new HashMap<>();
	private final Map<UUID, Tags<Toon>> toontTags = new HashMap<>();
	
	private final Map<UUID, ArcheAccount> pendingBlobs = new ConcurrentHashMap<>();
	
	public void implement(Player player) {
		UUID uuid = player.getUniqueId();
		ArcheAccount account = pendingBlobs.remove(uuid);
		if(account != null){ //Had something to be implemented (first login time only)
			accounts.put(uuid, account);
			accountsById.put(account.getId(), account);
			account.getToons().forEach(t->toons.put(uuid, t));
			
			if(accountTags.containsKey(account.getId()));
		}
	}
}
