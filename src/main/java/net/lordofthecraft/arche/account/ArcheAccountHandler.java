package net.lordofthecraft.arche.account;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.AccountHandler;
import net.lordofthecraft.arche.interfaces.Toon;

public class ArcheAccountHandler implements AccountHandler {
	private static final ArcheAccountHandler instance = new ArcheAccountHandler();
	
	private final Map<UUID, ArcheAccount> accounts = new HashMap<>();
	private final Map<Integer, ArcheAccount> accountsById = new HashMap<>();
	private final Map<UUID, ArcheToon> toons = new HashMap<>();
	
	private final Map<Integer, AgnosticTags<Account>> accountTags = new HashMap<>();
	private final Map<UUID, AgnosticTags<Toon>> toonTags = new HashMap<>();
	
	private final Map<UUID, ArcheAccount> pendingBlobs = new ConcurrentHashMap<>();
	
	public static ArcheAccountHandler getInstance() {
		return instance;
	}
	
	private ArcheAccountHandler() {
		//Do nothing
	}
	
	public void implement(Player player) {
		UUID uuid = player.getUniqueId();
		ArcheAccount account = pendingBlobs.remove(uuid);
		if(account != null){ //Had something to be implemented (first login time only)
			accounts.put(uuid, account);
			accountsById.put(account.getId(), account);
			account.getToons().forEach(t->toons.put(uuid, t));
			
			int aid = account.getId();
			var tags_acc = (AgnosticTags<Account>) account.getTags();
			if(accountTags.containsKey(aid)) tags_acc.merge(accountTags.get(aid));
			accountTags.put(aid, tags_acc);
			
			for(Toon t : account.getToons()) {
				UUID tid = t.getUniqueId();
				var tags_toon = (AgnosticTags<Toon>) t.getTags();
				if(toonTags.containsKey(tid)) tags_toon.merge(toonTags.get(tid));
				toonTags.put(tid, tags_toon);
			}
		}
	}
	
	public void initTags() {
		ResultSet rs;
		try(Connection c = ArcheCore.getSQLControls().getConnection(); Statement s = c.createStatement()){
			rs = s.executeQuery("SELECT * FROM account_tags");
			while(rs.next()) {
				int id = rs.getInt("account_id_fk");
				AgnosticTags<Account> t = accountTags.get(id);
				if(t == null) {
					t = new AgnosticTags<>(null, "account_tags", "account_id_fk", id);
					t.forOffline = true;
					accountTags.put(id, t);
				}
				t.putInternal(rs.getString(AbstractTags.TAG_KEY), rs.getString(AbstractTags.TAG_VALUE));
				
			}
			
			s.close();
			
			rs = s.executeQuery("SELECT * FROM toon_tags");
			while(rs.next()) {
				UUID id = UUID.fromString(rs.getString("player_fk"));
				AgnosticTags<Toon> t = toonTags.get(id);
				if(t == null) {
					t = new AgnosticTags<>(null, "account_tags", "account_id_fk", id);
					toonTags.put(id, t);
				}
				t.putInternal(rs.getString(AbstractTags.TAG_KEY), rs.getString(AbstractTags.TAG_VALUE));
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
