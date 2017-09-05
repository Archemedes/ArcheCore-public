package net.lordofthecraft.arche;

import com.google.common.collect.Maps;
import net.lordofthecraft.arche.SQL.SQLHandler;

import java.sql.SQLException;
import java.util.Map;

public final class ArcheTables {

    protected ArcheTables() {
        //Do nothing
    }

    public static void setUpSQLTables(SQLHandler sqlHandler) {
		createPlayerTable(sqlHandler);
		createPersonaTable(sqlHandler);
		createPersonaVitalsTable(sqlHandler);
		createPersonaStatsTable(sqlHandler);
		createPersonaTagsTable(sqlHandler);
		createSkillsTable(sqlHandler);
		createRacialSkillsTable(sqlHandler);
		createPersonaSkillsTable(sqlHandler);
		createPersonaNamesTable(sqlHandler);
		createPersonaSpawnsTable(sqlHandler);
		createBlockRegistryTable(sqlHandler);
		createPersonaSkinsTable(sqlHandler);
	}

    protected static void createPlayerTable(SQLHandler sqlHandler) {
        Map<String, String> cols = Maps.newLinkedHashMap();
		cols.put("player", "CHAR(36)");
		cols.put("force_preload", "BOOLEAN DEFAULT FALSE");
		cols.put("PRIMARY KEY (persona_id)", "");
		sqlHandler.createTable("players", cols);
	}

    protected static void createPersonaTable(SQLHandler sqlHandler) {
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
        cols.put("saturation", "DOUBLE DEFAULT 0");
        cols.put("PRIMARY KEY (persona_id_fk)", "");
		cols.put("FOREIGN KEY (persona_id_fk)", "REFERENCES persona (persona_id) ON UPDATE CASCADE");
		sqlHandler.createTable("persona_vitals", cols);
	}

    protected static void createPersonaTagsTable(SQLHandler sqlHandler) {
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

	protected static void createSkillsTable(SQLHandler sqlHandler) {
		Map<String, String> cols = Maps.newLinkedHashMap();
		cols.put("skill_id", "VARCHAR(255)");
		cols.put("hidden", "INT DEFAULT 0");
		cols.put("help_text", "TEXT");
		cols.put("help_icon", "TEXT");
		cols.put("inert", "BOOLEAN DEFAULT FALSE");
		cols.put("male_name", "TEXT");
		cols.put("female_name", "TEXT");
		cols.put("PRIMARY KEY (persona_id_fk,skill_id_fk)", "");
		sqlHandler.createTable("persona_skills", cols);
	}

	protected static void createRacialSkillsTable(SQLHandler sqlHandler) {
		Map<String, String> cols = Maps.newLinkedHashMap();
		cols.put("skill_id_fk", "VARCHAR(255)");
		cols.put("race", "TEXT NOT NULL");
		cols.put("racial_skill", "BOOLEAN DEFAULT FALSE");
		cols.put("racial_mod", "DOUBLE DEFAULT 1.0");
		cols.put("PRIMARY KEY (skill_id_fk,race)", "");
		cols.put("FOREIGN KEY (skill_id_fk)", "REFERENCES skills (skill_id) ON UPDATE CASCADE");
		sqlHandler.createTable("persona_skills", cols);
	}

	protected static void createPersonaSkillsTable(SQLHandler sqlHandler) {
		Map<String, String> cols = Maps.newLinkedHashMap();
		cols.put("persona_id_fk", "INT UNSIGNED");
		cols.put("skill_id_fk", "VARCHAR(255)");
		cols.put("xp", "DOUBLE DEFAULT 0.0");
		cols.put("visible", "BOOLEAN DEFAULT TRUE");
		cols.put("PRIMARY KEY (persona_id_fk,skill_id_fk)", "");
		cols.put("FOREIGN KEY (persona_id_fk)", "REFERENCES persona (persona_id) ON UPDATE CASCADE");
		cols.put("FOREIGN KEY (skill_id_fk)", "REFERENCES skills (skill_id) ON UPDATE CASCADE");
		sqlHandler.createTable("persona_skills", cols);
	}

    protected static void createPersonaNamesTable(SQLHandler sqlHandler) {
        Map<String,String> cols = Maps.newLinkedHashMap();
		cols.put("player", "TEXT NOT NULL");
		cols.put("id", "INT NOT NULL");
		cols.put("name", "TEXT NOT NULL");
		//cols.put("FOREIGN KEY (player, id)", "REFERENCES persona(player, id) ON DELETE CASCADE");
		cols.put("PRIMARY KEY (player,id,name)", "");
		sqlHandler.createTable("persona_names", cols);
	}

    protected static void createPersonaSpawnsTable(SQLHandler sqlHandler) {
        Map<String,String> cols = Maps.newLinkedHashMap();
		cols.put("race", "TEXT PRIMARY KEY");
		cols.put("world", "TEXT NOT NULL");
		cols.put("x", "INT NOT NULL");
		cols.put("y", "INT NOT NULL");
		cols.put("z", "INT NOT NULL");
		cols.put("yaw", "REAL");
		sqlHandler.createTable("persona_race_spawns", cols);
	}

    protected static void createBlockRegistryTable(SQLHandler sqlHandler) {
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

    protected static void createDeleteProcedure(SQLHandler handler) {
        try {
            handler.getConnection().createStatement().executeUpdate("DELIMITER $$" +
                    "CREATE PROCEDURE delete_persona(" +
                    "IN pers_id INT)" +
                    "LANGUAGE SQL" +
                    "DETERMINISTIC" +
                    "COMMENT 'Logs and deletes persona entries based off of a persona ID number.'" +
                    "BEGIN" +
                    "DECLARE p_name TEXT;" +
                    "DECLARE p_race TEXT;" +
                    "DECLARE p_st_played INT UNSIGNED;" +
                    "DECLARE p_st_char INT UNSIGNED;" +
                    "DECLARE p_st_played_old INT UNSIGNED;" +
                    "DECLARE p_st_renamed INT UNSIGNED;" +
                    "DECLARE p_date_created TIMESTAMP;" +
                    "DECLARE p_inv TEXT;" +
                    "DECLARE p_ender_inv TEXT;" +
                    "DECLARE mon_pers DOUBLE;" +
                    "" +
                    "DECLARE new_log_id INT UNSIGNED;" +
                    "" +
                    "SELECT persona_name INTO p_name FROM persona WHERE persona_id_fk=pers_id;" +
                    "SELECT race INTO p_race FROM persona WHERE persona_id=pers_id;" +
                    "SELECT played INTO p_st_played FROM persona_stats WHERE persona_id_fk=pers_id;" +
                    "SELECT chars INTO p_st_char FROM persona_stats WHERE persona_id_fk=pers_id;" +
                    "SELECT playtime_past INTO p_st_played_old FROM persona_stats WHERE persona_id_fk=pers_id;" +
                    "SELECT renamed INTO p_st_renamed FROM persona_stats WHERE persona_id_fk=pers_id;" +
                    "SELECT persona_stats.date_created INTO p_date_created FROM persona_stats WHERE persona_id_fk=pers_id;" +
                    "SELECT inv INTO p_inv FROM persona_vitals WHERE persona_id_fk=pers_id;" +
                    "SELECT money INTO mon_pers FROM persona WHERE persona_id_fk=pers_id;" +
                    "SELECT ender_inv INTO p_ender_inv FROM persona_vitals WHERE persona_id_fk=pers_id;" +
                    "" +
                    "INSERT INTO persona_log (player,id,persona_id,pers_name,race,date_created,date_removed,stat_played,stat_chars,stat_renamed,stat_playtime_past,inv,money_personal,money_bank) " +
                    "VALUES (pl, prid, pers_id, p_name, p_race, p_date_created, NOW(), p_st_played, p_st_char, p_st_renamed, p_st_played_old, p_inv, mon_pers);" +
                    "SELECT DISTINCT log_id INTO new_log_id FROM persona_log WHERE player=pl AND id=prid ORDER BY date_removed;" +
                    "" +
                    "DELETE FROM persona_stats WHERE persona_id_fk=pers_id;" +
                    "DELETE FROM persona_vitals WHERE persona_id_fk=pers_id;" +
                    "DELETE FROM persona_tags WHERE persona_id_fk=pers_id;" +
                    "DELETE FROM persona_skills WHERE persona_id_fk=pers_id;" +
                    "DELETE FROM persona_tags WHERE persona_id_fk=pers_id;" +
                    "" +
                    "DELETE FROM persona WHERE persona_id=pers_id;" +
                    "END $$" +
                    "DELIMITER ;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected static void createPersonaSkinsTable(SQLHandler sqlHandler) {
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
