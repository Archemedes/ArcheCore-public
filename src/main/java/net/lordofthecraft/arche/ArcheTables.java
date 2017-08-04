package net.lordofthecraft.arche;

import java.util.Map;

import com.google.common.collect.Maps;

import net.lordofthecraft.arche.SQL.SQLHandler;

public class ArcheTables {

	
	public static void setUpSQLTables(SQLHandler sqlHandler) {
		createPersonaTable(sqlHandler);
		createPersonaNamesTable(sqlHandler);
		createPersonaSpawnsTable(sqlHandler);
		createBlockRegistryTable(sqlHandler);
		createPersonaSkinsTable(sqlHandler);
	}
	
	
	private static void createPersonaTable(SQLHandler sqlHandler) {
		//Create the Persona table
		Map<String,String> cols = Maps.newLinkedHashMap();
		cols.put("player", "TEXT"); //1
		cols.put("id", "INT");
		cols.put("name", "TEXT");
		cols.put("age", "INT");
		cols.put("race", "TEXT"); //5
		cols.put("rheader", "TEXT");
		cols.put("gender", "INT");
		cols.put("desc", "TEXT");
		cols.put("prefix", "TEXT");
		cols.put("current", "INT DEFAULT 1"); //10
		cols.put("autoage", "INT DEFAULT 1");
		cols.put("stat_played", "INT DEFAULT 0");
		cols.put("stat_chars", "INT DEFAULT 0");
		cols.put("stat_renamed", "INT DEFAULT 0");
		cols.put("skill_xpgain", "INT DEFAULT 1"); //15
		cols.put("skill_selected", "TEXT");
		cols.put("world", "TEXT");
		cols.put("x", "INT");
		cols.put("y", "INT");
		cols.put("z", "INT"); //20
		//cols.put("health", "REAL");
		//cols.put("hunger", "REAL");
		//cols.put("saturation", "REAL");
		cols.put("inv", "TEXT");
		cols.put("money", "REAL DEFAULT 0");
		cols.put("skill_primary", "TEXT");
		cols.put("skill_secondary", "TEXT");
		cols.put("skill_tertiary", "TEXT");
		//cols.put("skin","TEXT");
		cols.put("stat_creation", "LONG");
		cols.put("stat_playtime_past","INT");
		cols.put("skindata","TEXT");
		cols.put("PRIMARY KEY (player, id)", "ON CONFLICT REPLACE");
		sqlHandler.createTable("persona", cols);
	}
	
	private static void createPersonaNamesTable(SQLHandler sqlHandler) {
		Map<String,String> cols = Maps.newLinkedHashMap();
		cols.put("player", "TEXT NOT NULL");
		cols.put("id", "INT NOT NULL");
		cols.put("name", "TEXT NOT NULL");
		//cols.put("FOREIGN KEY (player, id)", "REFERENCES persona(player, id) ON DELETE CASCADE");
		cols.put("UNIQUE (player, id, name)", "ON CONFLICT IGNORE");
		sqlHandler.createTable("persona_names", cols);
	}
	
	private static void createPersonaSpawnsTable(SQLHandler sqlHandler) {
		Map<String,String> cols = Maps.newLinkedHashMap();
		cols.put("race", "TEXT PRIMARY KEY");
		cols.put("world", "TEXT NOT NULL");
		cols.put("x", "INT NOT NULL");
		cols.put("y", "INT NOT NULL");
		cols.put("z", "INT NOT NULL");
		cols.put("yaw", "REAL");
		sqlHandler.createTable("persona_race_spawns", cols);
	}
	
	private static void createBlockRegistryTable(SQLHandler sqlHandler) {
		//Blockregistry persistence stuff
		Map<String,String> 	cols = Maps.newLinkedHashMap();
		cols.put("world", "TEXT NOT NULL");
		cols.put("x", "INT");
		cols.put("y", "INT");
		cols.put("z", "INT");
		cols.put("UNIQUE (world, x, y, z)", "ON CONFLICT IGNORE");
		sqlHandler.createTable("blockregistry", cols);

		sqlHandler.execute("DELETE FROM blockregistry WHERE ROWID IN (SELECT ROWID FROM blockregistry ORDER BY ROWID DESC LIMIT -1 OFFSET 5000)");
	}
	
	private static void createPersonaSkinsTable(SQLHandler sqlHandler) {
		//Skins table
		Map<String,String> 	cols = Maps.newLinkedHashMap();
		cols.put("player", "TEXT NOT NULL");
		cols.put("index", "INT");
		cols.put("skinUrl", "TEXT");
		cols.put("slim", "INT");
		cols.put("skinValue", "TEXT");
		cols.put("skinSignature", "TEXT");
		cols.put("refresh", "INT");
		cols.put("UNIQUE (player, index)", "ON CONFLICT REPLACE");
		sqlHandler.createTable("persona_skins", cols);

		cols = Maps.newLinkedHashMap();
		cols.put("player", "TEXT NOT NULL");
		cols.put("id", "INT NOT NULL");
		cols.put("index", "INT");
		cols.put("UNIQUE (player, id)", "ON CONFLICT REPLACE");
		sqlHandler.createTable("persona_skins", cols);
		
	}
	
}
