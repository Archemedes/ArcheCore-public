package net.lordofthecraft.arche;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;

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
    		String lower = name.toLowerCase();
    		nameToId.put(lower, uuid); //This can override previous instances of the mapping
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
    
    UUID getPlayerUUIDFromName(String playerName) {
    	String lower = playerName.toLowerCase();
    	return nameToId.get(lower);
    }
    
    void updateNameMap(@NonNull String n, @NonNull UUID u) {
    	Validate.notNull(n);
    	Validate.notNull(u);
    	
    	var lower = n.toLowerCase();
    	if(u.equals(nameToId.get(lower))) return; //Up to date
    	nameToId.put(lower, u);
    	
    	var l1 = idToName.get(u);
    	if(l1.contains(n)) { //Probably means player changed name to something else, then back
    		//Change ordering in the linked list AND in the SQLite table (by deleting and re-inserting... yeah its dumb)
    		l1.remove(n);
    		plugin.getConsumer().delete("players").where("player", u).where("player_name", n).queue();
    	}
    	
    	l1.add(n);
    	plugin.getConsumer().insert("players").where("player", u).where("player_name", n).queue();
    	//This doesn't update other players that might currently also have name "n" as their last known name.
    }
}
