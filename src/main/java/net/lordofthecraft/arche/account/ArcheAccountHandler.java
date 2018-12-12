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
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.AccountHandler;
import net.lordofthecraft.arche.interfaces.Tags;

public class ArcheAccountHandler implements AccountHandler {
	private static final ArcheAccountHandler instance = new ArcheAccountHandler();
	
	private final Map<UUID, ArcheAccount> accounts = new ConcurrentHashMap<>();
	private final Map<Integer, ArcheAccount> accountsById = new ConcurrentHashMap<>();
	
	public static ArcheAccountHandler getInstance() {
		return instance;
	}
	
	private ArcheAccountHandler() {
		//Do nothing
	}
	
	private int nextId() {
		return -1; //implemented
	}
	
	public void implement(Player player) {
		UUID uuid = player.getUniqueId();
		ArcheAccount account = pendingBlobs.remove(uuid);
		if(account != null){ //Had something to be implemented (first login time only)
			accounts.put(uuid, account);
			accountsById.put(account.getId(), account);
			
			int aid = account.getId();
			var tags_acc = (AgnosticTags<Account>) account.getTags();
			if(accountTags.containsKey(aid)) tags_acc.merge(accountTags.get(aid));
			accountTags.remove(aid);
			
			for(Toon t : account.getToons()) {
				UUID tid = t.getUniqueId();
				var tags_toon = (AgnosticTags<Toon>) t.getTags();
				if(toonTags.containsKey(tid)) tags_toon.merge(toonTags.get(tid));
				toonTags.remove(tid);
			}
		}
	}
	
	public void logMeIn(String playerName, UUID uuid) {
		ResultSet rs;
		try(Connection c = ArcheCore.getSQLControls().getConnection(); Statement s = c.createStatement()){
			rs = s.executeQuery("SELECT * FROM ");
			if(rs.next()) { //This means the Minecraft account is known to us (should be exactly 1 entry)
				int id = rs.getInt(1);
				s.close();
				rs = s.executeQuery("SELECT * FROM accounts WHERE account_id="+id); //Should exist
				rs.next(); //Always true
				
				
				s.close();
			} else { //This means the Minecraft account is NOT known. Associate this with Soul status

			}
			
			rs = s.executeQuery("SELECT account_id_fk FROM minecraft_toons");
			
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void merge(Account from, Account to) {
		ArcheAccount to2 = (ArcheAccount) to;
		
		//Must merge all names, tags, toons to new account_id_fk
		from.getToons().stream()
			.map(ArcheToon.class::cast)
			.forEach(t->{
				accounts.put(t.getUniqueId(), to2);
				t.account = to2;
			});
		
		from.getTags().getTags().stream()
			.filter(ta->!to.getTags().hasTag(ta.getKey()))
			.forEach(ta->to.getTags().giveTag(ta));
		
		accountsById.remove(from.getId());
		accountTags.remove(from.getId());
		
		var consumer = ArcheCore.getConsumerControls();
		consumer.update("minecraft_toons")
		.where("account_id_fk", from.getId())
		.set("account_id_fk", to.getId())
		.queue();

		consumer.delete("account_tags")
		.where("account_id_fk", from.getId())
		.queue();
		
		consumer.delete("accounts")
		.where("account_id", from.getId())
		.queue();
			
	}
	
/*	public Tags<Account> getAccountTags(int id){
		accountsById.containsKey(id) return accountsById.get(key)
	}

	public Tags<Account> getAccountTags(UUID uuid){

	}
	
	public Tags<Toon> getToonTags(UUID uuid){

	}*/


	public void init() {
		transition();
		initTags();
	}
	
	private void transition() {
	//Check if we're functioning from previous setup:
		ResultSet rs;
		try(Connection c = ArcheCore.getSQLControls().getConnection(); Statement s = c.createStatement()){
			rs = s.executeQuery("SELECT account_id FROM accounts");
			boolean weHaveAccounts = rs.next();
			if(!weHaveAccounts) {
				CoreLog.warning("There were NO accounts found in ArcheCore. Either you have no players or we just upgraded. "
						+ "Let's find out which by going through the namelog file (table name 'players')");
				s.close();
				rs = s.executeQuery("SELECT * FROM players");
				int handled = 0;
				while(rs.next()) {
					handled++;
					UUID uuid = UUID.fromString(rs.getString("player"));
					String name = rs.getString("player_name");
					ArcheAccount acc = new ArcheAccount(nextId());
					acc.addToon(uuid, name);
					
					accounts.put(uuid, acc);
					accountsById.put(acc.getId(), acc);
				}
				CoreLog.info("We've made new accounts for players, some of which might be alts. Handled in total: " + handled);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	private void initTags() {
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
