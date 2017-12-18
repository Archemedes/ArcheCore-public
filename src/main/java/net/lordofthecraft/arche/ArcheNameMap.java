package net.lordofthecraft.arche;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.lang.Validate;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.lordofthecraft.arche.save.rows.player.ReplacePlayerRow;
import net.lordofthecraft.arche.util.AsyncRunner;
import net.lordofthecraft.arche.util.MojangCommunicator;

public class ArcheNameMap {
    private final BiMap<UUID, CaseString> playerNameMap = HashBiMap.create();
    private final ArcheCore plugin;
    
    ArcheNameMap(ArcheCore plugin){
    	this.plugin = plugin;
    }
    
    void onEnable(Connection c) throws SQLException {
    	ArcheTimer timer = plugin.getMethodTimer();
        if (timer != null) timer.startTiming("Loading player UUID/name map");
     	ResultSet res = c.createStatement().executeQuery("SELECT player,player_name FROM players");
     	while(res.next()) {
     		UUID uuid = UUID.fromString(res.getString("player"));
     		CaseString name = new CaseString(res.getString("player_name"));
     		
     		if(playerNameMap.containsValue(name)) {
     			getLogger().warning("Duplicate UUID entries exist for username: " + name.value);
     			UUID otheruuid = playerNameMap.inverse().get(name);
     			getLogger().warning( String.format("Affected UUIDS: %s and %s", uuid, otheruuid) );
     			getLogger().warning("Talking to Mojang to update names. This is a one-time process");
     			try {
     				CaseString n1 = new CaseString(MojangCommunicator.requestCurrentUsername(uuid));
     				CaseString n2 = new CaseString(MojangCommunicator.requestCurrentUsername(otheruuid));

     				if(!name.equals(n2)) { //This replacement must be done first or it will still conflict with next insert.
     					getLogger().info(otheruuid + " was outdated. Setting new name: " + n2.value);
     					playerNameMap.put(otheruuid, n2);
     					plugin.getConsumer().queueRow(new ReplacePlayerRow(otheruuid, n2.value));
     				}
     				playerNameMap.put(uuid, n1); //Due to conflict we never mapped a name value to this uuid. So do this regardless
     				if(!name.equals(n1)) {
     					getLogger().info(uuid + " was outdated. Setting new name: " + n1.value);
     					plugin.getConsumer().queueRow(new ReplacePlayerRow(uuid, n1.value));
     				}
     			} catch(Exception e) {
     				e.printStackTrace();
     			}
     			
     		} else {
     			playerNameMap.put(uuid, name);
     		}
     		
     		
     	}
     	res.close();
     	res.getStatement().close();
     	if (timer != null) timer.stopTiming("Loading player UUID/name map");
    }
    
    String getPlayerNameFromUUID(UUID playerUUID) {
    	CaseString w = playerNameMap.get(playerUUID);
    	return w==null? null : w.getValue();
    }
    
    UUID getPlayerUUIDFromName(String playerName) {
    	CaseString w = new CaseString(playerName);
    	return playerNameMap.inverse().get(w);
    }
    
    void updateNameMap(String n, UUID u) {
    	CaseString caseString = new CaseString(n);
    	
    	if(playerNameMap.containsValue(caseString)) {
    		UUID o = playerNameMap.inverse().get(caseString);
    		if(!u.equals(o)) { //This name was used by a player with other UUID. We must update both
         		if(ArcheCore.isDebugging()) getLogger().info("[Debug] Updating CONFLICTING Player Name Map: " + u + "=" + n);
         		playerNameMap.forcePut(u, caseString); //Force-put means old entry is wiped
    			plugin.getConsumer().queueRow(new ReplacePlayerRow(u,n));
   

    			//This is done async and not instant. 
    			//Might lead to MySQL desyncs. This is why we sanitize during onEnable
    			updateMapFromMojang(o);
    		} else {//u == o. UUID u has the right name. Don't update.
    			return;
    		}
    	} else {
    		//There is no value linked to this name.
    		//Either this uuid is linked to a different name, or player uuid is not in map
    		//In both cases an update for this uuid is going to be needed
    		if(ArcheCore.isDebugging()) getLogger().info("[Debug] Updating Player Name Map: " + u + "=" + n);
    		playerNameMap.put(u, caseString);
        	plugin.getConsumer().queueRow(new ReplacePlayerRow(u, n));
    	}
    }
    
    //// Util Methods
    private Logger getLogger() {
    	return plugin.getLogger();
    }
    
    private void updateMapFromMojang(UUID uuid) {
    	new AsyncRunner(plugin) { //Here we async update the new name
			String newName;
			
			@Override 
			protected void doAsync() {
				try{
					newName = MojangCommunicator.requestCurrentUsername(uuid);
					if(ArcheCore.isDebugging()) getLogger().info("[Debug] New name obtained from mojang for " + uuid + ": " + newName);
				}catch(Exception e) {
					e.printStackTrace();
					newName = null;
				}
			}

			@Override 
			protected void andThen() {
				if(newName != null) updateNameMap(newName, uuid); //NB: Recursive
			}
		}.go();
    }
    
    //// Util class
    public class CaseString {
    	public final String value;
    	private final String value_lowercase;
    	
    	public CaseString(String value) {
    		Validate.notNull(value);
    		this.value = value;
    		this.value_lowercase = value.toLowerCase();
    	}
    	
    	@Override
    	public int hashCode() {
    		return value_lowercase.hashCode();
    	}
    	
    	@Override
    	public boolean equals(Object other) {
    		if(other == null || other.getClass() != this.getClass())
    			return false;
    		
    		return value_lowercase.equals(((CaseString) other).value_lowercase);
    	}
    	
    	public String getValue() {
    		return value;
    	}
    	
    	@Override
    	public String toString() {
    		return value;
    	}
    }
}
