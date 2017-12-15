package net.lordofthecraft.arche;

import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            if (debug) l.info("[Debug] Creating player table...");
            createPlayerTable(statement, end);
            if (debug) l.info("[Debug] Done with Player! Creating skins table...");
            createPersonaSkinsTable(statement, end);
            if (debug) l.info("[Debug] Done with Skins! Creating skills table...");
            createSkillsTable(statement, end);
            if (debug) l.info("[Debug] Done with skills! Creating persona...");
            createPersonaTable(statement, end);
            if (debug) l.info("[Debug] Done with persona! Creating persona stats...");
            createPersonaStatsTable(statement, end);
            if (debug) l.info("[Debug] Done with persona stats! Creating persona tags...");
            createPersonaTagsTable(statement, end);
            if (debug) l.info("[Debug] Done with persona tags! Creating persona vitals...");
            createPersonaVitalsTable(statement, end);
            if (debug) l.info("[Debug] Done with persona vitals! Creating persona attributes...");
            createPersonaAttributes(statement, end);
            if (debug) l.info("[Debug] Done with persona attributes! Creating racial skills...");
            createRacialSkillsTable(statement, end);
            if (debug) l.info("[Debug] Done with racial skills! Creating persona skills...");
            createPersonaSkillsTable(statement, end);
            if (debug) l.info("[Debug] Done with persona skills! Creating persona names...");
            createPersonaNamesTable(statement, end);
            if (debug) l.info("[Debug] Done with persona names! Creating persona spawns...");
            createPersonaSpawnsTable(statement, end);
            if (debug) l.info("[Debug] Done with persona spawns! Creating block registry...");
            createBlockRegistryTable(statement, end);

            if (sqlHandler instanceof WhySQLHandler) {
                //So as it turns out SQLite doesn't support stored procedures. This sucks, but in order to make sure that we're
                //not overburdening the consumer with massive logging statement after statement we're going to ONLY log
                //If we're using MySQL. Sorry SQLite fangays.

                //If it's SQLite check net.lordofthecraft.arche.save.rows.persona.delete.PersonaDeleteRow for how it's handled.
                if (debug) l.info("[Debug] Done with block registry! Creating logging tables...");
                createLoggingTables(statement, end, false);
                if (debug) l.info("[Debug] Done with logging tables! Creating delete procedure...");
                try {
                    createDeleteProcedure(statement);
                    ArcheCore.getPlugin().setUseDeleteProcedure(true);
                } catch (Exception e) {
                    l.warning("Failed to instantiate the delete procedure");
                }
            }
            createEconomyLogging(statement, end);
            if (debug) l.info("[Debug] Done with economy log! All done!");
            conn.commit();
            l.info("We've finished with committing all tables now.");
            statement.close();
            if (timer != null) {
                timer.stopTiming("Database Creation");
            }
            toreturn = true;
        } catch (Exception e) {
        	ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "We failed to create Archecore.db! Stopping!", e);
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

    protected static void createPlayerTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS players (" +
                "player CHAR(36)," +
                "player_name VARCHAR(16) DEFAULT NULL," +
                "PRIMARY KEY (player), UNIQUE (player_name)" +
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
                "refresh TIMESTAMP," +
                "PRIMARY KEY (player,slot)" +
                ")" +
                end);

    }


    protected static void createSkillsTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS skills (" +
                "skill_id VARCHAR(255)," +
                "hidden INT DEFAULT 0," +
                "help_text TEXT," +
                "help_icon TEXT," +
                "inert BOOLEAN DEFAULT FALSE," +
                "male_name TEXT," +
                "female_name TEXT," +
                "PRIMARY KEY (skill_id)" +
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
                "last_played TIMESTAMP," +
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
                "world CHAR(36) DEFAULT 'gofuckyourselfgofuckyourselfgofuckyo'," +
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
                "created TIMESTAMP," +
                "decayticks LONG," +
                "decaytype TEXT," +
                "lostondeath BOOLEAN DEFAULT FALSE," +
                "PRIMARY KEY (mod_uuid,persona_id_fk,attribute_type)," +
                "FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE CASCADE" +
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
                "date_created TIMESTAMP," +
                "played INT UNSIGNED DEFAULT 0," +
                "chars INT UNSIGNED DEFAULT 0," +
                "renamed TIMESTAMP," +
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
        statement.execute("CREATE TABLE IF NOT EXISTS persona_name (" +
                "persona_id_fk INT UNSIGNED," +
                "name TEXT NOT NULL," +
                "PRIMARY KEY (persona_id_fk)," +
                "FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE CASCADE" +
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
                "date TIMESTAMP," +
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
                "date TIMESTAMP," +
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
        statement.execute("CREATE TABLE IF NOT EXISTS persona_log (" +
                "persona_id INT UNSIGNED," +
                "removed_date TIMESTAMP," +
                "player_fk CHAR(36) NOT NULL," +
                "slot INT UNSIGNED NOT NULL," +
                "race VARCHAR(255) NOT NULL," +
                "name TEXT," +
                "race_header TEXT DEFAULT NULL," +
                "gender TEXT DEFAULT 'Other'," +
                "p_type TEXT DEFAULT 'NORMAL'," +
                "descr TEXT DEFAULT NULL," +
                "prefix TEXT DEFAULT NULL," +
                "curr BOOLEAN DEFAULT FALSE," +
                "money DOUBLE DEFAULT 0.0," +
                "skin INT DEFAULT -1," +
                "profession VARCHAR(255) DEFAULT NULL," +
                "fatigue DOUBLE DEFAULT 0.0," +
                "max_fatigue DOUBLE DEFAULT 100.00," +
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
                "renamed TIMESTAMP," +
                "playtime_past INT UNSIGNED DEFAULT 0," +
                "date_created TIMESTAMP," +
                "last_played TIMESTAMP," +
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

        statement.execute("CREATE TABLE IF NOT EXISTS persona_tags_log (" +
                "persona_id_fk INT UNSIGNED," +
                "tag_key VARCHAR(255) NOT NULL," +
                "tag_value TEXT," +
                "PRIMARY KEY (persona_id_fk,tag_key)," +
                "FOREIGN KEY (persona_id_fk) REFERENCES persona_log (persona_id)" +
                ")" +
                end);

        statement.execute("CREATE TABLE IF NOT EXISTS persona_attributes_log (" +
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
                end);

    }

    protected static void createDeleteProcedure(Statement statement) throws SQLException {
        statement.execute(
                "CREATE PROCEDURE delete_persona(IN pers_id CHAR(36)) " +
                        "LANGUAGE SQL " +
                        "DETERMINISTIC " +
                        "COMMENT 'Logs and deletes persona entries based off of a persona ID number.' " +
                        "BEGIN " +
                        "INSERT INTO persona_log(persona_id,player_fk,slot,race,name,race_header,gender,p_type,descr,prefix,curr,money,skin,profession,fatigue,world,x,y,z,inv,ender_inv,potions,health,hunger,saturation,played,chars,renamed,playtime_past,date_created,last_played) " +
                        "SELECT persona_id,player_fk,slot,race,name,race_header,gender,p_type,descr,prefix,curr,money,skin,profession,fatigue,world,x,y,z,inv,ender_inv,health,hunger,saturation,played,chars,renamed,playtime_past,date_created,last_played " +
                        "FROM persona JOIN persona_vitals ON persona.persona_id = persona_vitals.persona_id_fk " +
                        "JOIN persona_stats ON persona.persona_id = persona_stats.persona_id_fk " +
                        "WHERE persona.persona_id = pers_id; " +
                        "INSERT INTO persona_skills_log(persona_id_fk,skill_id_fk,xp,visible) SELECT persona_id_fk,skill_id_fk,xp,visible FROM persona_skills WHERE persona_id_fk = pers_id; " +
                        "INSERT INTO persona_tags_log(persona_id_fk,tag_key,tag_value) SELECT persona_id_fk,tag_key,tag_value FROM persona_tags WHERE persona_id_fk = pers_id; " +
                        "INSERT INTO persona_attributes_log(mod_uuid,persona_id_fk,attribute_type,mod_name,mod_value,operation,created,decaytime) SELECT mod_uuid,persona_id_fk,attribute_type,mod_name,operation,created,decaytime FROM persona_attributes WHERE persona_id_fk = pers_id; " +
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
