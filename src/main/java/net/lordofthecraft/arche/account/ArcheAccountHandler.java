package net.lordofthecraft.arche.account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import lombok.var;
import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.CoreLog;
import net.lordofthecraft.arche.interfaces.Account;
import net.lordofthecraft.arche.interfaces.AccountHandler;
import net.lordofthecraft.arche.interfaces.OfflinePersona;
import net.lordofthecraft.arche.interfaces.Persona;
import net.lordofthecraft.arche.persona.ArchePersonaHandler;

public class ArcheAccountHandler implements AccountHandler {
	private static final ArcheAccountHandler instance = new ArcheAccountHandler();
	
	private final Map<UUID, ArcheAccount> accounts = new HashMap<>();
	private final Map<Integer, ArcheAccount> accountsById = new HashMap<>();
	private int max_account_id = 0;
	private final Loader loader = new Loader(this, ArchePersonaHandler.getInstance());
	
	public static ArcheAccountHandler getInstance() {
		return instance;
	}
	
	private ArcheAccountHandler() {
		//Do nothing
	}
	
	private int getNextAccountId() {
		return max_account_id++;
	}
	
	
	@Override
	public Account getAccount(UUID u) {
		return accounts.get(u);
	}
	
	@Override
	public Account getAccount(int id) {
		return accountsById.get(id);
	}
	
	@Override
	public boolean isLoaded(UUID u) {
		return loader.isLoaded(u);
	}
	
	@Override
	public Waiter<Account> loadAccount(UUID u){
		if(ArcheCore.getControls().getPlayerNameFromUUID(u) != null) {
			//This player is KNOWN to ArcheCore --> account should therefore exist
			return loader.check(u);
		} else {
			throw new IllegalArgumentException("UUID is not known to ArcheCore: " + u);
		}
	}
	
	public Waiter<Persona> loadPersona(OfflinePersona op){
		return loader.check(op);
	}
	
	public void load(UUID uuid, boolean createIfAbsent) {
		loader.initialize(uuid);
	}
	
	public void implement(ArcheAccount account) {
		account.getUUIDs().stream().forEach(u->accounts.put(u, account));
		accountsById.put(account.getId(), account);
	}
	
	public ArcheAccount fetchAccount(UUID uuid) { //Thread-safe
		ArcheAccount account = null;
		
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
				
			} else { //Account doesn't exist. We must create it
				account = makeAccount(uuid);
			}
			rs.close();
		}catch(SQLException e) {
			throw new RuntimeException(e);
		}
		
		return account;
	}

	private ArcheAccount makeAccount(UUID uuid) {
		ArcheAccount account = new ArcheAccount(getNextAccountId());
		account.alts.add(uuid);
		var cons = ArcheCore.getConsumerControls();
		int id = account.getId();
		cons.insert("accounts").set("account_id", id).queue();
		cons.insert("playeraccounts").set("player", uuid).set("account_id_fk", id).queue();
		return account;
	}
	
	public void init() {
		getMaxId();
		transition();
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
	
	public void merge(ArcheAccount from, ArcheAccount to) {
		to.merge(from);
		from.getUUIDs().forEach(u->accounts.put(u, to));
		accountsById.remove(from.getId());
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
				int made = 0;
				while(rs.next()) {
					handled++;
					UUID uuid = UUID.fromString(rs.getString("player"));
					if(!accounts.containsKey(uuid)) {
						made++;
						ArcheAccount acc = makeAccount(uuid);
						accounts.put(uuid, acc);
						accountsById.put(acc.getId(), acc);
					}
				}
				CoreLog.info("We've made new accounts for players, some of which might be alts. Names scanned: " + handled + ". Accounts made: " + made);
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
}
