package net.lordofthecraft.arche;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.SQL.SQLHandler;

import java.util.Map;

public class ArcheTables {

	
	public static void setUpSQLTables(SQLHandler sqlHandler) {
		createPlayerTable(sqlHandler);
		createPersonaTable(sqlHandler);
		createPersonaVitalsTable(sqlHandler);
		createPersonaStatsTable(sqlHandler);
		createPersonaTagsTable(sqlHandler);
		createPersonaNamesTable(sqlHandler);
		createPersonaSpawnsTable(sqlHandler);
		createBlockRegistryTable(sqlHandler);
		createPersonaSkinsTable(sqlHandler);
	}

	private static void createPlayerTable(SQLHandler sqlHandler) {
		Map<String, String> cols = Maps.newLinkedHashMap();
		cols.put("player", "CHAR(36)");
		cols.put("force_preload", "BOOLEAN DEFAULT FALSE");
		cols.put("PRIMARY KEY (persona_id)", "");
		sqlHandler.createTable("players", cols);
	}

	private static void createPersonaTable(SQLHandler sqlHandler) {
		//Create the Persona table
		Map<String,String> cols = Maps.newLinkedHashMap();
		cols.put("persona_id", "INT UNSIGNED AUTO_INCREMENT");
		cols.put("player_fk", "CHAR(36) NOT NULL");
		cols.put("slot", "INT UNSIGNED NOT NULL");
		cols.put("name", "TEXT");
		cols.put("race_key", "VARCHAR(255) NOT NULL");
		cols.put("race_header", "TEXT DEFAULT NULL");
		cols.put("gender", "INT UNSIGNED DEFAULT 2");
		cols.put("p_type", "TEXT DEFAULT 'NORMAL'");
		cols.put("descr", "TEXT DEFAULT NULL");
		cols.put("prefix", "TEXT DEFAULT NULL");
		cols.put("curr", "BOOLEAN DEFAULT FALSE");
		cols.put("money", "DOUBLE DEFAULT 0.0");
		cols.put("skin", "TEXT DEFAULT NULL");
		cols.put("profession", "TEXT DEFAULT NULL");
		cols.put("fatigue", "DOUBLE DEFAULT 0.0");
		cols.put("max_fatigue", "DOUBLE DEFAULT 100.00");
		cols.put("PRIMARY KEY (persona_id)", "");
		cols.put("FOREIGN KEY (player_fk)", "REFERENCES players (player)");
		sqlHandler.createTable("persona", cols);
	}

	private static void createPersonaVitalsTable(SQLHandler sqlHandler) {
		Map<String, String> cols = Maps.newLinkedHashMap();
		cols.put("persona_id_fk", "INT UNSIGNED");
		cols.put("world", "VARCHAR(255) NOT NULL");
		cols.put("x", "INT NOT NULL");
		cols.put("y", "INT NOT NULL");
		cols.put("z", "INT NOT NULL");
		cols.put("inv", "TEXT");
		cols.put("ender_inv", "TEXT");
		cols.put("health", "DOUBLE DEFAULT 10.0");
		cols.put("hunger", "INT DEFAULT 20");
		cols.put("saturation", "INT DEFAULT 0");
		cols.put("PRIMARY KEY (persona_id_fk)", "");
		cols.put("FOREIGN KEY (persona_id_fk)", "REFERENCES persona (persona_id) ON UPDATE CASCADE");
		sqlHandler.createTable("persona_vitals", cols);
	}

	private static void createPersonaTagsTable(SQLHandler sqlHandler) {
		Map<String, String> cols = Maps.newLinkedHashMap();
		cols.put("persona_id_fk", "INT UNSIGNED");
		cols.put("key", "TEXT NOT NULL");
		cols.put("value", "TEXT");
		cols.put("PRIMARY KEY (persona_id_fk,key)", "");
		cols.put("FOREIGN KEY (persona_id_fk)", "REFERENCES persona (persona_id)");
		sqlHandler.createTable("persona_tags", cols);
	}

	protected static void createPersonaStatsTable(SQLHandler sqlHandler) {
		Map<String, String> cols = Maps.newLinkedHashMap();
		cols.put("persona_id_fk", "INT UNSIGNED");
		cols.put("played", "INT UNSIGNED DEFAULT 0");
		cols.put("chars", "INT UNSIGNED DEFAULT 0");
		cols.put("renamed", "TIMESTAMP DEFAULT NOW()");
		cols.put("playtime_past", "INT UNSIGNED DEFAULT 0");
		cols.put("date_created", "TIMESTAMP NOT NULL DEFAULT NOW()");
		cols.put("last_played", "TIMESTAMP DEFAULT NOW()");
		cols.put("PRIMARY KEY (persona_id_fk)", "");
		cols.put("FOREIGN KEY (persona_id_fk)", "REFERENCES persona (persona_id) ON UPDATE CASCADE");
		sqlHandler.createTable("persona_stats", cols);
	}
	
	private static void createPersonaNamesTable(SQLHandler sqlHandler) {
		Map<String,String> cols = Maps.newLinkedHashMap();
		cols.put("player", "TEXT NOT NULL");
		cols.put("id", "INT NOT NULL");
		cols.put("name", "TEXT NOT NULL");
		//cols.put("FOREIGN KEY (player, id)", "REFERENCES persona(player, id) ON DELETE CASCADE");
		cols.put("PRIMARY KEY (player,id,name)", "");
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
		cols.put("PRIMARY KEY (world, x, y, z)", "");
		sqlHandler.createTable("blockregistry", cols);

		sqlHandler.execute("DELETE FROM blockregistry WHERE ROWID IN (SELECT ROWID FROM blockregistry ORDER BY ROWID DESC LIMIT -1 OFFSET 5000)");
	}
	
	private static void createPersonaSkinsTable(SQLHandler sqlHandler) {
		//Skins table
		Map<String,String> 	cols = Maps.newLinkedHashMap();
		cols.put("player", "TEXT NOT NULL");
		cols.put("slot", "INT");
		cols.put("name", "TEXT");
		cols.put("skinUrl", "TEXT");
		cols.put("slim", "INT");
		cols.put("skinValue", "TEXT");
		cols.put("skinSignature", "TEXT");
		cols.put("refresh", "INT");
		//TODO proper update syntax
		cols.put("PRIMARY KEY (player, slot)", "");
		sqlHandler.createTable("persona_skins", cols);

		cols = Maps.newLinkedHashMap();
		cols.put("player", "TEXT NOT NULL");
		cols.put("id", "INT NOT NULL");
		cols.put("slot", "INT");
		//TODO update syntax
		cols.put("PRIMARY KEY (player, id)", "");
		sqlHandler.createTable("persona_skins_used", cols);
		
	}
	
}
