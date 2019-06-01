package net.lordofthecraft.arche;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;

import lombok.NonNull;
import lombok.var;

public class ArcheNameMap {
		private final ListMultimap<UUID, String> idToName = MultimapBuilder.hashKeys().linkedListValues().build();
		private final Map<String, UUID> nameToId = new LinkedHashMap<>();
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
    		String name = res.getString("player_name");
    		idToName.put(uuid, name);
    		nameToId.put(name.toLowerCase(), uuid); //This can override previous instances of the mapping
    	}
     	res.close();
     	res.getStatement().close();
    }
    
    String getPlayerNameFromUUID(@NonNull UUID playerUUID) {
    	List<String> list = idToName.get(playerUUID);
    	if(list.isEmpty()) return null;
    	return list.get(list.size() - 1);
    }
    
    List<String> getKnownAliases(@NonNull UUID playerUUID){
    	return Collections.unmodifiableList(idToName.get(playerUUID));
    }
    
    UUID getPlayerUUIDFromAlias(@NonNull String playerName) {
    	return nameToId.get(playerName.toLowerCase());
    }
    
    UUID getPlayerUUIDFromName(@NonNull String playerName) {
    	UUID u = getPlayerUUIDFromAlias(playerName);
    	if(u == null) return null;
    	//We have a uuid linked but it might be outdated!
    	//only return it if it matches the most recent name of this uuid
    	String currentName = getPlayerNameFromUUID(u);
    	if(playerName.equalsIgnoreCase(currentName)) return u;
    	else return null;
    }
    
    void updateNameMap(@NonNull String n, @NonNull UUID u) {
    	var lower = n.toLowerCase();
    	boolean insert = false; //Must we update the name map
    	
    	if(!u.equals(nameToId.get(lower))) {
    		nameToId.put(lower, u);
    		insert = true;
    	}
    	
    	var l1 = idToName.get(u);
    	int nameIndex = l1.indexOf(n);
    	int bottomSpot = l1.size() - 1;
    	if( nameIndex == -1 || nameIndex != bottomSpot ) {
    		//It must be updated
    		insert = true;
    		//If name was in another slot of the list we must change ordering in the SQLite table (by deleting and re-inserting)
    		if(l1.remove(n)) plugin.getConsumer().delete("players").where("player", u).where("player_name", n).queue();
    		l1.add(n);
    	}
    	
    	if(insert) plugin.getConsumer().insert("players").where("player", u).where("player_name", n).queue();
    	//This doesn't update other players that might currently also have name "n" as their last known name.
    }
}
