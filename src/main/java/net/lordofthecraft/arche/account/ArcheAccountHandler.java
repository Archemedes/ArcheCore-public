package net.lordofthecraft.arche.account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.AccountHandler;

public class ArcheAccountHandler implements AccountHandler {
	private static final ArcheAccountHandler instance = new ArcheAccountHandler();
	
	private final Map<UUID, ArcheAccount> accounts = new ConcurrentHashMap<>();
	private int max_account_id = 0;
	
	public static ArcheAccountHandler getInstance() {
		return instance;
	}
	
	private ArcheAccountHandler() {
		//Do nothing
	}
	
	private int getNextAccountId() {
		return max_account_id++;
	}
	
	public void joinPlayer(Player player) {
		
	}
	
	public ArcheAccount fetchAccount(UUID uuid, boolean createIfAbsent) {
		//If Account was already loaded, just return it from cache
		if(accounts.containsKey(uuid)) return accounts.get(uuid);
		
		ArcheAccount account = null;
		boolean mustInsert = false;
		
		ResultSet rs;
		try(Connection c = ArcheCore.getSQLControls().getConnection(); Statement s = c.createStatement()){
			rs = s.executeQuery("SELECT * FROM playeraccounts WHERE player='" + uuid.toString() + "' LIMIT 1");
			if(rs.next()) { //Account exists (should be 1 at most). Load it
				var id = rs.getInt("account_id_fk");
				rs.close();
				
				//Make the account itself
				rs = s.executeQuery("SELECT * FROM accounts WHERE account_id="+id);
				var forumId = rs.getLong("forum_id");
				var discordId = rs.getLong("discord_id");
				account = new ArcheAccount(id, forumId, discordId);
				account.lastSeen = rs.getDate("last_seen");
				account.timePlayed = rs.getLong("time_played");
				rs.close();
				
				//UUIDs added
				rs = s.executeQuery("SELECT player FROM playeraccounts WHERE account_id_fk="+id);
				while(rs.next()) account.alts.add(UUID.fromString(rs.getString("player")));
				rs.close();
				
				//Tags added
				rs = s.executeQuery("SELECT * FROM account_tags WHERE account_id_fk="+id);
				AgnosticTags<Account> t = (AgnosticTags<Account>) account.getTags();
				while(rs.next()) t.putInternal(rs.getString(AbstractTags.TAG_KEY), rs.getString(AbstractTags.TAG_VALUE));
				rs.close();
				
				//IPs added
				rs = s.executeQuery("SELECT ip_address FROM account_ips WHERE account_id_fk="+id);
				while(rs.next()) account.ips.add(rs.getString("ip_address"));
				rs.close();
				
			} else if(createIfAbsent){ //Account doesn't exist. We must create it?
				account = new ArcheAccount(getNextAccountId(), 0, 0);
				account.alts.add(uuid);
				mustInsert = true;
			}
		}catch(SQLException e) {
			throw new RuntimeException(e);
		}
		
		//Check if other threads beat us to loading the account
		ArcheAccount other = accounts.putIfAbsent(uuid, account);
		if(other != null) account = other;
		else if(mustInsert) {
			var c = ArcheCore.getConsumerControls();
			int id = account.getId();
			c.insert("accounts").set("account_id", id).queue();
			c.insert("playeraccounts").set("player", uuid).set("account_id_fk", id).queue();
		}
		return account;
	}
	
/*	public void merge(Account from, Account to) {
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
			
	}*/
	
/*	public Tags<Account> getAccountTags(int id){
		accountsById.containsKey(id) return accountsById.get(key)
	}

	public Tags<Account> getAccountTags(UUID uuid){

	}
	
	public Tags<Toon> getToonTags(UUID uuid){

	}*/


	public void init() {
		getMaxId();
		//transition();
	}
	
	private void getMaxId() {
		try(Connection connection = ArcheCore.getSQLControls().getConnection();
				PreparedStatement statement = connection.prepareStatement("SELECT MAX(persona_id) AS 'max_persona_id' FROM persona");
				ResultSet rs = statement.executeQuery();)
		{
			if (rs.next()) {
				max_account_id = rs.getInt(1);
				max_account_id++;
			} else {
        CoreLog.warning("There are no accounts or there is an error talking to database.");
        CoreLog.warning(" We'll be starting at 0. This will cause errors if there are any pre-existing accounts");
			}
		}catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
/*	private void transition() {
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
	}*/
}
