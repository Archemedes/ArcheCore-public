package net.lordofthecraft.arche;

import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;
import net.lordofthecraft.arche.util.extension.ArcheExtension;
import net.lordofthecraft.arche.util.extension.UtilExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.experimental.ExtensionMethod;

public final class ArcheTables {

	protected ArcheTables() {
		//Do nothing
	}

	public static boolean setUpSQLTables(SQLHandler sqlHandler) {
		Connection conn = sqlHandler.getConnection();
		boolean toreturn = true;
		try {
			ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
			boolean debug = timer != null;
			if (debug) {
				timer.startTiming("Database Creation");
			}

			conn.setAutoCommit(false);
			Statement statement = conn.createStatement();

			String end = getEndingString(sqlHandler);

			Logger l = ArcheCore.getPlugin().getLogger();
			l.fine("Creating player account tables...");
			createAccountTable(statement, end);
			createAccountTagsTable(statement, end);
			createAccountPlayerTable(statement, end);
			l.fine("Creating name and ip logging tables...");
			createPlayerTable(statement, end);
			createIpTable(statement, end);
			l.fine("Done with accounts! Creating skins table...");
			createPersonaSkinsTable(statement, end);
			l.fine("Done with skins! Creating persona...");
			createPersonaTable(statement, end);
			l.fine("Done with persona! Creating persona stats...");
			createPersonaStatsTable(statement, end);
			l.fine("Done with persona stats! Creating persona tags...");
			createPersonaTagsTable(statement, end);
			l.fine("Done with persona tags! Creating persona vitals...");
			createPersonaVitalsTable(statement, end);
			l.fine("Done with persona vitals! Creating persona attributes...");
			createPersonaAttributes(statement, end);
			l.fine("Done with persona attributes! Creating persona play sessions...");
			createPersonaSessions(statement, end);
			l.fine("Done with persona attributes! Creating racial skills...");
			createRacialSkillsTable(statement, end);
			l.fine("Done with racial skills! Creating persona skills...");
			createPersonaSkillsTable(statement, end);
			l.fine("Done with persona skills! Creating persona names...");
			createPersonaNamesTable(statement, end);
			l.fine("Done with persona names! Creating persona spawns...");
			createPersonaSpawnsTable(statement, end);
			l.fine("Done with persona spawns! Creating block registry...");
			createBlockRegistryTable(statement, end);

			if (sqlHandler instanceof WhySQLHandler) {
				//So as it turns out SQLite doesn't support stored procedures. This sucks, but in order to make sure that we're
				//not overburdening the consumer with massive logging statement after statement we're going to ONLY log
				//If we're using MySQL. Sorry SQLite fangays.

				//If it's SQLite check net.lordofthecraft.arche.save.rows.persona.delete.PersonaDeleteRow for how it's handled.
				l.fine("Done with block registry! Creating logging tables...");
				createLoggingTables(statement, end, false);
				l.fine("Done with logging tables! Creating delete procedure...");
				try {
					createDeleteProcedure(statement);
					ArcheCore.getPlugin().setUseDeleteProcedure(true);
				} catch (Exception e) {
					l.warning("Failed to instantiate the delete procedure");
				}
			}
			createEconomyLogging(statement, end);
			l.fine("Done with economy log! All done!");
			conn.commit();
			l.info("We've finished with committing all tables now.");
			statement.close();
			if (timer != null) {
				timer.stopTiming("Database Creation");
			}
			toreturn = true;
		} catch (Exception e) {
			CoreLog.log(Level.SEVERE, "We failed to create Archecore.db! Stopping!", e);
			e.printStackTrace();
			toreturn = false;
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return toreturn;
	}

	protected static void createAccountTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS accounts (" +
				"account_id INT UNSIGNED," +
				"forum_id INT UNSIGNED UNIQUE DEFAULT 0," +
				"discord_id INT UNSIGNED UNIQUE DEFAULT 0," +
				"time_played INT UNSIGNED DEFAULT 0," +
				"last_seen DATETIME(3)," +
				"PRIMARY KEY (account_id)" +
				")" +
				end);
	}
	
	protected static void createAccountPlayerTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS playeraccounts (" +
				"player CHAR(36)," +
				"account_id_fk INT UNSIGNED," +
				"PRIMARY KEY (player)" +
				")" +
				end);
		
		statement.execute("CREATE INDEX IF NOT EXISTS idx_player_accountid ON playeraccounts (account_id_fk)" + end);
		
	}
	
	protected static void createAccountTagsTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS account_tags (" +
				"account_id_fk INT UNSIGNED," +
				"tag_key VARCHAR(255) NOT NULL," +
				"tag_value TEXT," +
				"offline BOOLEAN DEFAULT FALSE," +
				"PRIMARY KEY (account_id_fk,tag_key)," +
				"FOREIGN KEY (account_id_fk) REFERENCES account (account_id) ON UPDATE CASCADE" +
				")" +
				end);
	}

	protected static void createPlayerTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS players (" +
				"player CHAR(36)," +
				"player_name VARCHAR(16) DEFAULT NULL," +
				"PRIMARY KEY (player)" +
				")" +
				end);
	}
	
	protected static void createIpTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS account_ips (" +
				"account_id_fk INT UNSIGNED," +
				"ip_address VARCHAR(64) NOT NULL," +
				"PRIMARY KEY (account_id_fk,ip_address)," +
				"FOREIGN KEY (account_id_fk) REFERENCES account (account_id) ON UPDATE CASCADE" +
				")" +
				end);
	}

	protected static void createPersonaSkinsTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS persona_skins (" +
				"player CHAR(36) NOT NULL," +
				"slot INT," +
				"name TEXT," +
				"skinUrl TEXT," +
				"slim BOOLEAN DEFAULT FALSE," +
				"skinValue TEXT," +
				"skinSignature TEXT," +
				//TODO update
				"refresh DATETIME(3)," +
				"PRIMARY KEY (player,slot)" +
				")" +
				end);

	}

	protected static void createPersonaTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS persona (" +
				"persona_id INT UNSIGNED," +
				"player_fk CHAR(36) NOT NULL," +
				"slot INT UNSIGNED NOT NULL," +
				"race VARCHAR(255) NOT NULL," +
				"name TEXT," +
				"race_header TEXT DEFAULT NULL," +
				"birthdate INT DEFAULT 0," +
				"gender VARCHAR(255) DEFAULT 'Other'," +
				"p_type VARCHAR(16) DEFAULT 'NORMAL'," +
				"descr TEXT DEFAULT NULL," +
				"prefix TEXT DEFAULT NULL," +
				"curr BOOLEAN DEFAULT FALSE," +
				"money DOUBLE DEFAULT " + ArcheCore.getEconomyControls().getBeginnerAllowance() + "," +
				"profession VARCHAR(255) DEFAULT NULL," +
				"fatigue DOUBLE DEFAULT 0.0," +
				"last_played DATETIME(3)," +
				"skin_slot INT DEFAULT -1," +
				"PRIMARY KEY (persona_id)," +
				"FOREIGN KEY (player_fk) REFERENCES players (player) ON UPDATE CASCADE ON DELETE RESTRICT," +
				"FOREIGN KEY (profession) REFERENCES skills (skill_id) ON UPDATE CASCADE ON DELETE SET NULL" +
				")" +
				end);
	}

	protected static void createPersonaVitalsTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS persona_vitals (" +
				"persona_id_fk INT UNSIGNED," +
				"world CHAR(36)," +
				"x INT NOT NULL," +
				"y INT NOT NULL," +
				"z INT NOT NULL," +
				"inv TEXT," +
				"ender_inv TEXT," +
				"potions TEXT DEFAULT NULL," +
				"health DOUBLE DEFAULT 20.0," +
				"hunger INT DEFAULT 20," +
				"saturation FLOAT DEFAULT 0.0," +
				"PRIMARY KEY (persona_id_fk)," +
				"FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE RESTRICT" +
				")" +
				end);
	}

	protected static void createPersonaAttributes(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS persona_attributes (" +
				"mod_uuid CHAR(36)," +
				"persona_id_fk INT UNSIGNED," +
				"attribute_type VARCHAR(255)," +
				"mod_name TEXT NOT NULL," +
				"mod_value DOUBLE DEFAULT 0.0," +
				"operation TEXT NOT NULL," +
				"created DATETIME(3)," +
				"decayticks LONG," +
				"decaytype TEXT," +
				"lostondeath BOOLEAN DEFAULT FALSE," +
				"PRIMARY KEY (mod_uuid,persona_id_fk,attribute_type)," +
				"FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE CASCADE" +
				")" +
				end);
	}

	protected static void createPersonaSessions(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS persona_sessions (" +
				"persona_id_fk INT UNSIGNED," +
				"login LONG," +
				"logout LONG," +
				"world TEXT," +
				"x INT," +
				"y INT," +
				"z INT," +
				"time_played INT" +
				")" +
				end);
	}


	protected static void createPersonaTagsTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS persona_tags (" +
				"persona_id_fk INT UNSIGNED," +
				"tag_key VARCHAR(255) NOT NULL," +
				"tag_value TEXT," +
				"offline BOOLEAN DEFAULT FALSE," +
				"PRIMARY KEY (persona_id_fk,tag_key)," +
				"FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id)" +
				")" +
				end);
	}

	protected static void createPersonaStatsTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS persona_stats (" +
				"persona_id_fk INT UNSIGNED," +
				//TODO Update
				"date_created DATETIME(3)," +
				"played INT UNSIGNED DEFAULT 0," +
				"chars INT UNSIGNED DEFAULT 0," +
				//TODO update
				"renamed DATETIME(3)," +
				"playtime_past INT UNSIGNED DEFAULT 0," +
				"PRIMARY KEY (persona_id_fk)," +
				"FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE RESTRICT" +
				")" +
				end);
	}

	protected static void createRacialSkillsTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS skill_races (" +
				"skill_id_fk VARCHAR(255)," +
				"race VARCHAR(255) NOT NULL," +
				"racial_skill BOOLEAN DEFAULT FALSE," +
				"racial_mod DOUBLE DEFAULT 1.0," +
				"PRIMARY KEY (skill_id_fk,race)," +
				"FOREIGN KEY (skill_id_fk) REFERENCES skills (skill_id) ON UPDATE CASCADE ON DELETE CASCADE" +
				")" +
				end);
	}

	protected static void createPersonaSkillsTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS persona_skills (" +
				"persona_id_fk INT UNSIGNED," +
				"skill_id_fk VARCHAR(255)," +
				"xp DOUBLE DEFAULT 0.0," +
				"visible BOOLEAN DEFAULT TRUE," +
				"PRIMARY KEY (persona_id_fk,skill_id_fk)," +
				"FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE RESTRICT," +
				"FOREIGN KEY (skill_id_fk) REFERENCES skills (skill_id) ON UPDATE CASCADE ON DELETE RESTRICT" +
				")" +
				end);
	}

	protected static void createPersonaNamesTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS persona_names (" +
				"persona_id_fk INT UNSIGNED," +
				"name VARCHAR(16) NOT NULL" +
				")" +
				end);
	}

	protected static void createPersonaSpawnsTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS persona_race_spawns (" +
				"race VARCHAR(255)," +
				"world TEXT NOT NULL," +
				"x INT NOT NULL," +
				"y INT NOT NULL," +
				"z INT NOT NULL," +
				"yaw REAL," +
				"PRIMARY KEY (race)" +
				")" +
				end);
	}

	protected static void createBlockRegistryTable(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS blockregistry (" +
				//TODO Update?
				"date DATETIME(3)," +
				"world CHAR(36) NOT NULL," +
				"x INT," +
				"y INT," +
				"z INT," +
				"data TEXT DEFAULT NULL," +
				"PRIMARY KEY (world,x,y,z)" +
				")" +
				end);
	}

	protected static void createEconomyLogging(Statement statement, String end) throws SQLException {
		statement.execute("CREATE TABLE IF NOT EXISTS econ_log(" +
				//TODO Update
				"date DATETIME(3)," +
				"persona_id_fk INT UNSIGNED," +
				"type VARCHAR(255)," +
				"amount DOUBLE," +
				"plugin TEXT," +
				"reason TEXT," +
				"amt_before DOUBLE," +
				"amt_after DOUBLE," +
				"FOREIGN KEY (persona_id_fk) REFERENCES persona(persona_id) ON UPDATE CASCADE ON DELETE SET NULL" +
				")" +
				end);
	}

	protected static void createLoggingTables(Statement statement, String end, boolean sqlite) throws SQLException {
		/*
                statement.execute("CREATE TABLE IF NOT EXISTS persona (" +
                "persona_id INT UNSIGNED," +
                "player_fk CHAR(36) NOT NULL," +
                "slot INT UNSIGNED NOT NULL," +
                "race VARCHAR(255) NOT NULL," +
                "name TEXT," +
                "race_header TEXT DEFAULT NULL," +
                "birthdate INT DEFAULT 0," +
                "gender VARCHAR(255) DEFAULT 'Other'," +
                "p_type VARCHAR(16) DEFAULT 'NORMAL'," +
                "descr TEXT DEFAULT NULL," +
                "prefix TEXT DEFAULT NULL," +
                "curr BOOLEAN DEFAULT FALSE," +
                "money DOUBLE DEFAULT " + ArcheCore.getEconomyControls().getBeginnerAllowance() + "," +
                "profession VARCHAR(255) DEFAULT NULL," +
                "fatigue DOUBLE DEFAULT 0.0," +
                "last_played TIMESTAMP," +
                "skin_slot INT DEFAULT -1," +
                "PRIMARY KEY (persona_id)," +
                "FOREIGN KEY (player_fk) REFERENCES players (player) ON UPDATE CASCADE ON DELETE RESTRICT," +
                "FOREIGN KEY (profession) REFERENCES skills (skill_id) ON UPDATE CASCADE ON DELETE SET NULL" +
                ")" +
                end);
		 */
		statement.execute("CREATE TABLE IF NOT EXISTS persona_log (" +
				"persona_id INT UNSIGNED," +
				//TODO Update
				"removed_date DATETIME(3)," +
				"player_fk CHAR(36) NOT NULL," +
				"slot INT UNSIGNED NOT NULL," +
				"race VARCHAR(255) NOT NULL," +
				"name TEXT," +
				"race_header TEXT DEFAULT NULL," +
				"birthdate INT DEFAULT 0," +
				"gender VARCHAR(255) DEFAULT 'Other'," +
				"p_type VARCHAR(16) DEFAULT 'NORMAL'," +
				"descr TEXT DEFAULT NULL," +
				"prefix TEXT DEFAULT NULL," +
				"curr BOOLEAN DEFAULT FALSE," +
				"money DOUBLE DEFAULT " + ArcheCore.getEconomyControls().getBeginnerAllowance() + "," +
				"skin_slot INT DEFAULT -1," +
				"profession VARCHAR(255) DEFAULT NULL," +
				"fatigue DOUBLE DEFAULT 0.0," +
				"world VARCHAR(255) NOT NULL," +
				"x INT NOT NULL," +
				"y INT NOT NULL," +
				"z INT NOT NULL," +
				"inv TEXT," +
				"ender_inv TEXT," +
				"potions TEXT," +
				"health DOUBLE DEFAULT 10.0," +
				"hunger INT DEFAULT 20," +
				"saturation FLOAT DEFAULT 0.0," +
				"played INT UNSIGNED DEFAULT 0," +
				"chars INT UNSIGNED DEFAULT 0," +
				"renamed DATETIME(3)," +
				"playtime_past INT UNSIGNED DEFAULT 0," +
				"date_created DATETIME(3)," +
				"last_played DATETIME(3)," +
				"PRIMARY KEY (persona_id)" +
				")" +
				end);

		statement.execute("CREATE TABLE IF NOT EXISTS persona_skills_log (" +
				"persona_id_fk INT UNSIGNED," +
				"skill_id_fk VARCHAR(255)," +
				"xp DOUBLE DEFAULT 0.0," +
				"visible BOOLEAN DEFAULT TRUE," +
				"PRIMARY KEY (persona_id_fk,skill_id_fk)," +
				"FOREIGN KEY (persona_id_fk) REFERENCES persona_log (persona_id) ON UPDATE CASCADE ON DELETE RESTRICT" +
				")" +
				end);

		/*
        statement.execute("CREATE TABLE IF NOT EXISTS persona_tags (" +
                "persona_id_fk INT UNSIGNED," +
                "tag_key VARCHAR(255) NOT NULL," +
                "tag_value TEXT," +
                "offline BOOLEAN DEFAULT FALSE," +
                "PRIMARY KEY (persona_id_fk,tag_key)," +
                "FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id)" +
                ")" +
                end);
		 */
		statement.execute("CREATE TABLE IF NOT EXISTS persona_tags_log (" +
				"persona_id_fk INT UNSIGNED," +
				"tag_key VARCHAR(255) NOT NULL," +
				"tag_value TEXT," +
				"offline BOOLEAN DEFAULT FALSE," +
				"PRIMARY KEY (persona_id_fk,tag_key)," +
				"FOREIGN KEY (persona_id_fk) REFERENCES persona_log (persona_id)" +
				")" +
				end);

		/*statement.execute("CREATE TABLE IF NOT EXISTS persona_attributes_log (" +
                "mod_uuid CHAR(36)," +
                "persona_id_fk INT UNSIGNED," +
                "attribute_type VARCHAR(255)," +
                "mod_name TEXT NOT NULL," +
                "mod_value DOUBLE DEFAULT 0.0," +
                "operation TEXT NOT NULL," +
                "created TIMESTAMP," +
                "decaytime TIMESTAMP," +
                "PRIMARY KEY (mod_uuid,persona_id_fk,attribute_type)," +
                "FOREIGN KEY (persona_id_fk) REFERENCES persona_log (persona_id) ON UPDATE CASCADE ON DELETE CASCADE" +
                ")" +
                end);*/

	}

	protected static void createDeleteProcedure(Statement statement) throws SQLException {


		statement.execute(
				"CREATE PROCEDURE delete_persona(IN pers_id CHAR(36)) " +
						"LANGUAGE SQL " +
						"DETERMINISTIC " +
						"COMMENT 'Logs and deletes persona entries based off of a persona ID number.' " +
						"BEGIN " +
						"INSERT INTO persona_log(persona_id,player_fk,slot,race,name,race_header,birthdate,gender,p_type,descr,prefix,curr,money,skin_slot,profession,fatigue,world,x,y,z,inv,ender_inv,potions,health,hunger,saturation,played,chars,renamed,playtime_past,date_created,last_played) " +
						"SELECT persona_id,player_fk,slot,race,name,race_header,birthdate,gender,p_type,descr,prefix,curr,money,skin_slot,profession,fatigue,world,x,y,z,inv,ender_inv,potions,health,hunger,saturation,played,chars,renamed,playtime_past,date_created,last_played " +
						"FROM persona JOIN persona_vitals ON persona.persona_id = persona_vitals.persona_id_fk " +
						"JOIN persona_stats ON persona.persona_id = persona_stats.persona_id_fk " +
						"WHERE persona.persona_id = pers_id; " +
						"INSERT INTO persona_skills_log(persona_id_fk,skill_id_fk,xp,visible) SELECT persona_id_fk,skill_id_fk,xp,visible FROM persona_skills WHERE persona_id_fk = pers_id; " +
						"INSERT INTO persona_tags_log(persona_id_fk,tag_key,tag_value,offline) SELECT persona_id_fk,tag_key,tag_value,offline FROM persona_tags WHERE persona_id_fk = pers_id; " +
						/*"INSERT INTO persona_attributes_log(mod_uuid,persona_id_fk,attribute_type,mod_name,mod_value,operation,created,decaytime) SELECT mod_uuid,persona_id_fk,attribute_type,mod_name,operation,created,decaytime FROM persona_attributes WHERE persona_id_fk = pers_id; " +*/
						"DELETE FROM persona_stats WHERE persona_id_fk = pers_id; " +
						"DELETE FROM persona_vitals WHERE persona_id_fk = pers_id; " +
						"DELETE FROM persona_tags WHERE persona_id_fk = pers_id; " +
						"DELETE FROM persona_skills WHERE persona_id_fk = pers_id; " +
						"DELETE FROM persona_attributes WHERE persona_id_fk = pers_id; " +
						"DELETE FROM persona WHERE persona_id = pers_id; " +
				"END");
	}

	private static String getEndingString(SQLHandler handler) {
		if (handler instanceof ArcheSQLiteHandler) {
			return ";";
		} else {
			return "ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		}
	}
}
