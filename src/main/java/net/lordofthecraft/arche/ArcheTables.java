package net.lordofthecraft.arche;

import net.lordofthecraft.arche.SQL.ArcheSQLiteHandler;
import net.lordofthecraft.arche.SQL.SQLHandler;
import net.lordofthecraft.arche.SQL.WhySQLHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public final class ArcheTables {

    protected ArcheTables() {
        //Do nothing
    }

    public static void setUpSQLTables(SQLHandler sqlHandler) {
        try {
            ArcheTimer timer = ArcheCore.getPlugin().getMethodTimer();
            if (timer != null) {
                timer.startTiming("Database Creation");
            }
            Connection conn = sqlHandler.getConnection();
            conn.setAutoCommit(false);
            Statement statement = conn.createStatement();

            String end = getEndingString(sqlHandler);
            //String auto = sqlHandler instanceof ArcheSQLiteHandler ? "AUTOINCREMENT" : "AUTO_INCREMENT";

            ArcheCore.getPlugin().getLogger().info("Creating player table...");
            createPlayerTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with Player! Creating skins table...");
            createPersonaSkinsTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with Skins! Creating skills table...");
            createSkillsTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with skills! Creating persona...");
            createPersonaTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with persona! Creating persona stats...");
            createPersonaStatsTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with persona stats! Creating persona tags...");
            createPersonaTagsTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with persona tags! Creating archetypes...");
            createArchetypeTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with archetypes! Creating magics...");
            createMagicTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with magics! Creating weaknesses....");
            createMagicWeaknesses(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with weaknesses! Creating creatures...");
            createCreaturesTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with creatures! Creating creature creators...");
            createCreatureCreators(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with creature creators! Creating creature abilities...");
            createCreatureAbilities(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with creature abilities! Creating persona vitals...");
            createPersonaVitalsTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with persona vitals! Creating persona attributes...");
            createPersonaAttributes(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with persona attributes! Creating racial skills...");
            createRacialSkillsTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with racial skills! Creating persona skills...");
            createPersonaSkillsTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with persona skills! Creating persona magics...");
            createPersonaMagicsTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with persona magics! Creating persona names...");
            createPersonaNamesTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with persona names! Creating persona spawns...");
            createPersonaSpawnsTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with persona spawns! Creating block registry...");
            createBlockRegistryTable(statement, end);
            ArcheCore.getPlugin().getLogger().info("Done with block registry! All done!");
            conn.commit();
            ArcheCore.getPlugin().getLogger().info("We've finished with committing all tables now.");
            statement.close();
            if (sqlHandler instanceof WhySQLHandler) {
                conn.setAutoCommit(true);
                //conn.close();
            } else {
                conn.setAutoCommit(true);
            }
            if (timer != null) {
                timer.stopTiming("Database Creation");
            }
        } catch (Exception e) {
            ArcheCore.getPlugin().getLogger().log(Level.SEVERE, "We failed to create Archecore.db! Stopping!", e);
            //Bukkit.getPluginManager().disablePlugin(ArcheCore.getPlugin());
        }

	}

    protected static void createPlayerTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS players (" +
                "player CHAR(36)," +
                "force_preload BOOLEAN DEFAULT FALSE," +
                "PRIMARY KEY (player)" +
                ")" +
                end);
    }


    protected static void createPersonaSkinsTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS persona_skins (" +
                "skin_id INT AUTO_INCREMENT," +
                "player CHAR(36) NOT NULL," +
                "slot INT," +
                "name TEXT," +
                "skinUrl TEXT," +
                "slim BOOLEAN DEFAULT FALSE," +
                "skinValue TEXT," +
                "skinSignature TEXT," +
                "refresh TIMESTAMP," +
                "PRIMARY KEY (skin_id)" +
                ")" +
                end);
        //Skins table
        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("skin_id", "INT AUTO_INCREMENT");
        cols.put("player", "CHAR(36) NOT NULL");
        cols.put("slot", "INT");
        cols.put("name", "TEXT");
        cols.put("skinUrl", "TEXT");
        cols.put("slim", "BOOLEAN DEFAULT FALSE");
        cols.put("skinValue", "TEXT");
        cols.put("skinSignature", "TEXT");
        cols.put("refresh", "TIMESTAMP");
        cols.put("PRIMARY KEY (skin_id)", "");
        sqlHandler.createTable("persona_skins", cols);*/

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

        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("skill_id", "VARCHAR(255)");
        cols.put("hidden", "INT DEFAULT 0");
        cols.put("help_text", "TEXT");
        cols.put("help_icon", "TEXT");
        cols.put("inert", "BOOLEAN DEFAULT FALSE");
        cols.put("male_name", "TEXT");
        cols.put("female_name", "TEXT");
        cols.put("PRIMARY KEY (skill_id)", "");
        sqlHandler.createTable("skills", cols);*/
    }

    protected static void createPersonaTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS persona (" +
                "persona_id CHAR(36)," +
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
                "PRIMARY KEY (persona_id)," +
                "FOREIGN KEY (player_fk) REFERENCES players (player) ON UPDATE CASCADE ON DELETE RESTRICT," +
                "FOREIGN KEY (profession) REFERENCES skills (skill_id) ON UPDATE CASCADE ON DELETE SET NULL," +
                "FOREIGN KEY (skin) REFERENCES persona_skins (skin_id) ON UPDATE CASCADE ON DELETE SET NULL" +
                ")" +
                end);
        //Create the Persona table
        /*Map<String,String> cols = Maps.newLinkedHashMap();
        cols.put("persona_id", "INT UNSIGNED AUTO_INCREMENT");
		cols.put("player_fk", "CHAR(36) NOT NULL");
		cols.put("slot", "INT UNSIGNED NOT NULL");
		cols.put("race_key", "VARCHAR(255) NOT NULL");
        cols.put("name", "TEXT");
        cols.put("race_header", "TEXT DEFAULT NULL");
        cols.put("gender", "TEXT DEFAULT 'Other'");
        cols.put("p_type", "TEXT DEFAULT 'NORMAL'");
		cols.put("descr", "TEXT DEFAULT NULL");
		cols.put("prefix", "TEXT DEFAULT NULL");
		cols.put("curr", "BOOLEAN DEFAULT FALSE");
		cols.put("money", "DOUBLE DEFAULT 0.0");
        cols.put("skin", "INT DEFAULT -1");
        cols.put("profession", "VARCHAR(255) DEFAULT NULL");
        cols.put("fatigue", "DOUBLE DEFAULT 0.0");
		cols.put("max_fatigue", "DOUBLE DEFAULT 100.00");
		cols.put("PRIMARY KEY (persona_id)", "");
        cols.put("FOREIGN KEY (player_fk)", "REFERENCES players (player) ON UPDATE CASCADE ON DELETE RESTRICT");
        cols.put("FOREIGN KEY (profession)", "REFERENCES skills (skill_id) ON UPDATE CASCADE ON DELETE SET NULL");
        cols.put("FOREIGN KEY (skin)", "REFERENCES persona_skins (skin_id) ON UPDATE CASCADE ON DELETE SET NULL");
        sqlHandler.createTable("persona", cols);*/
    }

    protected static void createArchetypeTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS magic_archetypes (" +
                "id_key VARCHAR(255)," +
                "name TEXT NOT NULL," +
                "parent_type VARCHAR(255) DEFAULT NULL," +
                "descr TEXT DEFAULT NULL," +
                "PRIMARY KEY (id_key)," +
                "FOREIGN KEY (parent_type) REFERENCES magic_archetypes (id_key) ON UPDATE CASCADE ON DELETE RESTRICT" +
                ")" +
                end);

        //statement.executeUpdate("ALTER TABLE magic_archetypes ADD CONSTRAINT fk_parent_type FOREIGN KEY (parent_type) REFERENCES magic_archetypes (id_key) ON UPDATE CASCADE ON DELETE RESTRICT");
        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("id_key", "VARCHAR(255)");
        cols.put("name", "TEXT NOT NULL");
        cols.put("parent_type", "VARCHAR(255) DEFAULT NULL");
        cols.put("descr", "TEXT DEFAULT NULL");
        cols.put("PRIMARY KEY (id_key)", "");
        sqlHandler.createTable("magic_archetypes", cols);

        //Might work. Might not. Uncertain.
        sqlHandler.execute("ALTER TABLE magic_archetypes ADD CONSTRAINT fk_parent_type FOREIGN KEY (parent_type) REFERENCES magic_archetypes (id_key) ON UPDATE CASCADE ON DELETE RESTRICT");*/
    }

    protected static void createMagicTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS magics (" +
                "id_key VARCHAR(255)," +
                "max_tier INT DEFAULT 5," +
                "extra_tier BOOLEAN DEFAULT FALSE," +
                "self_teach BOOLEAN DEFAULT FALSE," +
                "teachable BOOLEAN DEFAULT TRUE," +
                "description TEXT DEFAULT NULL," +
                "label TEXT NOT NULL," +
                "days_to_max INT UNSIGNED DEFAULT 120," +
                "days_to_extra INT UNSIGNED DEFAULT 0," +
                "archetype VARCHAR(255) NOT NULL," +
                "PRIMARY KEY (id_key)," +
                "FOREIGN KEY (archetype) REFERENCES magic_archetypes (id_key) ON UPDATE CASCADE ON DELETE RESTRICT" +
                ")" +
                end);
        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("id_key", "VARCHAR(255)");
        cols.put("max_tier", "INT DEFAULT 5");
        cols.put("extra_tier", "BOOLEAN DEFAULT FALSE");
        cols.put("self_teach", "BOOLEAN DEFAULT FALSE");
        cols.put("teachable", "BOOLEAN DEFAULT TRUE");
        cols.put("description", "TEXT DEFAULT NULL");
        cols.put("label", "TEXT NOT NULL");
        cols.put("days_to_max", "INT UNSIGNED DEFAULT 120");
        cols.put("days_to_extra", "INT UNSIGNED DEFAULT 0");
        cols.put("archetype", "VARCHAR(255) NOT NULL");
        cols.put("PRIMARY KEY (id_key)", "");
        cols.put("FOREIGN KEY (archetype)", "REFERENCES magic_archetypes (id_key) ON UPDATE CASCADE ON DELETE RESTRICT");
        sqlHandler.createTable("magics", cols);*/
    }

    protected static void createCreaturesTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS magic_creatures (" +
                "id_key VARCHAR(255)," +
                "name TEXT NOT NULL," +
                "descr TEXT DEFAULT NULL," +
                "PRIMARY KEY (id_key)" +
                ")" +
                end);
        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("id_key", "VARCHAR(255)");
        cols.put("name", "TEXT NOT NULL");
        cols.put("descr", "TEXT DEFAULT NULL");
        cols.put("PRIMARY KEY (id_key)", "");
        sqlHandler.createTable("magic_creatures", cols);*/
    }

    protected static void createMagicWeaknesses(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS magic_weaknesses (" +
                "fk_source_magic VARCHAR(255)," +
                "fk_weakness_magic VARCHAR(255)," +
                "modifier FLOAT DEFAULT 1.0," +
                "PRIMARY KEY (fk_source_magic,fk_weakness_magic)," +
                "FOREIGN KEY (fk_source_magic) REFERENCES magics (id_key) ON UPDATE CASCADE ON DELETE CASCADE," +
                "FOREIGN KEY (fk_weakness_magic) REFERENCES magics (id_key) ON UPDATE CASCADE ON DELETE CASCADE" +
                ")" +
                end);
        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("fk_source_magic", "VARCHAR(255)");
        cols.put("fk_weakness_magic", "VARCHAR(255)");
        cols.put("modifier", "FLOAT DEFAULT 1.0");
        cols.put("PRIMARY KEY (fk_source_magic,fk_weakness_magic)", "");
        cols.put("FOREIGN KEY (fk_source_magic)", "REFERENCES magics (id_key) ON UPDATE CASCADE ON DELETE CASCADE");
        cols.put("FOREIGN KEY (fk_weakness_magic)", "REFERENCES magics (id_key) ON UPDATE CASCADE ON DELETE CASCADE");
        sqlHandler.createTable("magic_archetypes", cols);*/
    }

    protected static void createCreatureCreators(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS creature_creators (" +
                "magic_id_fk VARCHAR(255)," +
                "creature_fk VARCHAR(255)," +
                "PRIMARY KEY (magic_id_fk,creature_fk)," +
                "FOREIGN KEY (magic_id_fk) REFERENCES magics (id_key) ON UPDATE CASCADE ON DELETE CASCADE," +
                "FOREIGN KEY (creature_fk) REFERENCES magic_creatures (id_key) ON UPDATE CASCADE ON DELETE CASCADE" +
                ")" +
                end);
        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("magic_id_fk", "VARCHAR(255)");
        cols.put("creature_fk", "VARCHAR(255)");
        cols.put("PRIMARY KEY (magic_id_fk,creature_fk)", "");
        cols.put("FOREIGN KEY (magic_id_fk)", "REFERENCES magics (id_key) ON UPDATE CASCADE ON DELETE CASCADE");
        cols.put("FOREIGN KEY (creature_fk)", "REFERENCES magic_creatures (id_key) ON UPDATE CASCADE ON DELETE CASCADE");
        sqlHandler.createTable("creature_creators", cols);*/
    }

    protected static void createCreatureAbilities(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS creature_abilities (" +
                "creature_fk VARCHAR(255)," +
                "ability VARCHAR(255)," +
                "PRIMARY KEY (creature_fk,ability)," +
                "FOREIGN KEY (creature_fk) REFERENCES magic_creatures (id_key) ON UPDATE CASCADE ON DELETE CASCADE" +
                ")" +
                end);
        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("creature_fk", "VARCHAR(255)");
        cols.put("ability", "VARCHAR(255)");
        cols.put("PRIMARY KEY (creature_fk,ability)", "");
        cols.put("FOREIGN KEY (creature_fk)", "REFERENCES magic_creatures (id_key) ON UPDATE CASCADE ON DELETE CASCADE");
        sqlHandler.createTable("creature_abilities", cols);*/
    }

    protected static void createPersonaVitalsTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS persona_vitals (" +
                "persona_id_fk CHAR(36)," +
                "world VARCHAR(255) NOT NULL," +
                "x INT NOT NULL," +
                "y INT NOT NULL," +
                "z INT NOT NULL," +
                "inv TEXT," +
                "ender_inv TEXT," +
                "health DOUBLE DEFAULT 10.0," +
                "hunger INT DEFAULT 20," +
                "saturation FLOAT DEFAULT 0.0," +
                "creature VARCHAR(255) DEFAULT NULL," +
                "PRIMARY KEY (persona_id_fk)," +
                "FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE RESTRICT," +
                "FOREIGN KEY (creature) REFERENCES magic_creatures (id_key) ON UPDATE CASCADE ON DELETE RESTRICT" +
                ")" +
                end);
        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("persona_id_fk", "INT UNSIGNED");
		cols.put("world", "VARCHAR(255) NOT NULL");
		cols.put("x", "INT NOT NULL");
		cols.put("y", "INT NOT NULL");
		cols.put("z", "INT NOT NULL");
		cols.put("inv", "TEXT");
		cols.put("ender_inv", "TEXT");
		cols.put("health", "DOUBLE DEFAULT 10.0");
		cols.put("hunger", "INT DEFAULT 20");
        cols.put("saturation", "FLOAT DEFAULT 0.0");
        cols.put("creature", "VARCHAR(255) DEFAULT NULL");
        cols.put("PRIMARY KEY (persona_id_fk)", "");
        cols.put("FOREIGN KEY (persona_id_fk)", "REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE RESTRICT");
        cols.put("FOREIGN KEY (creature)", "REFERENCES magic_creatures (id_key) ON UPDATE CASCADE ON DELETE RESTRICT");
        sqlHandler.createTable("persona_vitals", cols);*/
    }

    protected static void createPersonaAttributes(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS persona_attributes (" +
                "mod_uuid CHAR(36)," +
                "persona_id_fk CHAR(36)," +
                "attribute_type VARCHAR(255)," +
                "mod_name TEXT NOT NULL," +
                "mod_value DOUBLE DEFAULT 0.0," +
                "operation TEXT NOT NULL," +
                "created TIMESTAMP," +
                "decaytime TIMESTAMP DEFAULT NULL," +
                "PRIMARY KEY (mod_uuid,persona_id_fk,attribute_type)," +
                "FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE CASCADE" +
                ")" +
                end);
    }

    protected static void createPersonaTagsTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS persona_tags (" +
                "persona_id_fk CHAR(36)," +
                "tag_key VARCHAR(255) NOT NULL," +
                "tag_value TEXT," +
                "PRIMARY KEY (persona_id_fk,tag_key)," +
                "FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id)" +
                ")" +
                end);
        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("persona_id_fk", "INT UNSIGNED");
		cols.put("key", "TEXT NOT NULL");
		cols.put("value", "TEXT");
		cols.put("PRIMARY KEY (persona_id_fk,key)", "");
		cols.put("FOREIGN KEY (persona_id_fk)", "REFERENCES persona (persona_id)");
		sqlHandler.createTable("persona_tags", cols);*/
    }

    protected static void createPersonaStatsTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS persona_stats (" +
                "persona_id_fk CHAR(36)," +
                "played INT UNSIGNED DEFAULT 0," +
                "chars INT UNSIGNED DEFAULT 0," +
                "renamed TIMESTAMP," +
                "playtime_past INT UNSIGNED DEFAULT 0," +
                "date_created TIMESTAMP," +
                "last_played TIMESTAMP," +
                "PRIMARY KEY (persona_id_fk)," +
                "FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE RESTRICT" +
                ")" +
                end);
        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("persona_id_fk", "INT UNSIGNED");
		cols.put("played", "INT UNSIGNED DEFAULT 0");
		cols.put("chars", "INT UNSIGNED DEFAULT 0");
		cols.put("renamed", "TIMESTAMP DEFAULT NOW()");
		cols.put("playtime_past", "INT UNSIGNED DEFAULT 0");
		cols.put("date_created", "TIMESTAMP NOT NULL DEFAULT NOW()");
		cols.put("last_played", "TIMESTAMP DEFAULT NOW()");
		cols.put("PRIMARY KEY (persona_id_fk)", "");
		cols.put("FOREIGN KEY (persona_id_fk)", "REFERENCES persona (persona_id) ON UPDATE CASCADE");
		sqlHandler.createTable("persona_stats", cols);*/
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
        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("skill_id_fk", "VARCHAR(255)");
        cols.put("race", "VARCHAR(255) NOT NULL");
        cols.put("racial_skill", "BOOLEAN DEFAULT FALSE");
		cols.put("racial_mod", "DOUBLE DEFAULT 1.0");
		cols.put("PRIMARY KEY (skill_id_fk,race)", "");
		cols.put("FOREIGN KEY (skill_id_fk)", "REFERENCES skills (skill_id) ON UPDATE CASCADE");
        sqlHandler.createTable("skill_races", cols);*/
    }

    protected static void createPersonaMagicsTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS persona_magics (" +
                "magic_fk VARCHAR(255)," +
                "persona_id_fk CHAR(36)," +
                "tier INT DEFAULT 0," +
                "last_advanced TIMESTAMP," +
                "teacher CHAR(36)," +
                "learned TIMESTAMP," +
                "visible BOOLEAN DEFAULT TRUE," +
                "PRIMARY KEY (magic_fk,persona_id_fk)," +
                "FOREIGN KEY (magic_fk) REFERENCES magics (id_key) ON UPDATE CASCADE ON DELETE RESTRICT," +
                "FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE RESTRICT," +
                "FOREIGN KEY (teacher) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE SET NULL" +
                ")" +
                end);
        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("magic_fk", "VARCHAR(255)");
        cols.put("persona_id_fk", "INT UNSIGNED");
        cols.put("tier", "INT DEFAULT 0");
        cols.put("last_advanced", "TIMESTAMP DEFAULT NOW()");
        cols.put("teacher", "INT DEFAULT -1");
        cols.put("learned", "TIMESTAMP DEFAULT NOW()");
        cols.put("visible", "BOOLEAN DEFAULT TRUE");
        cols.put("PRIMARY KEY (magic_fk,persona_id_fk)", "");
        cols.put("FOREIGN KEY (magic_fk)", "REFERENCES magics (id_key) ON UPDATE CASCADE");
        cols.put("FOREIGN KEY (persona_id_fk)", "REFERENCES persona (persona_id) ON UPDATE CASCADE");
        sqlHandler.createTable("persona_magics", cols);*/
    }

    protected static void createPersonaSkillsTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS persona_skills (" +
                "persona_id_fk CHAR(36)," +
                "skill_id_fk VARCHAR(255)," +
                "xp DOUBLE DEFAULT 0.0," +
                "visible BOOLEAN DEFAULT TRUE," +
                "PRIMARY KEY (persona_id_fk,skill_id_fk)," +
                "FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE RESTRICT," +
                "FOREIGN KEY (skill_id_fk) REFERENCES skills (skill_id) ON UPDATE CASCADE ON DELETE RESTRICT" +
                ")" +
                end);
        /*Map<String, String> cols = Maps.newLinkedHashMap();
        cols.put("persona_id_fk", "INT UNSIGNED");
		cols.put("skill_id_fk", "VARCHAR(255)");
		cols.put("xp", "DOUBLE DEFAULT 0.0");
		cols.put("visible", "BOOLEAN DEFAULT TRUE");
		cols.put("PRIMARY KEY (persona_id_fk,skill_id_fk)", "");
		cols.put("FOREIGN KEY (persona_id_fk)", "REFERENCES persona (persona_id) ON UPDATE CASCADE");
		cols.put("FOREIGN KEY (skill_id_fk)", "REFERENCES skills (skill_id) ON UPDATE CASCADE");
		sqlHandler.createTable("persona_skills", cols);*/
    }

    protected static void createPersonaNamesTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS persona_name (" +
                "persona_id_fk CHAR(36)," +
                "name TEXT NOT NULL," +
                "PRIMARY KEY (persona_id_fk)," +
                "FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE CASCADE" +
                ")" +
                end);
        /*Map<String,String> cols = Maps.newLinkedHashMap();
        cols.put("persona_id_fk", "INT UNSIGNED");
        cols.put("name", "TEXT NOT NULL");
        //
        cols.put("PRIMARY KEY (persona_id_fk)", "");
        cols.put("FOREIGN KEY (persona_id_fk)", "REFERENCES persona(persona_id) ON UPDATE CASCADE ON DELETE CASCADE");
        sqlHandler.createTable("persona_names", cols);*/
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
        /*Map<String,String> cols = Maps.newLinkedHashMap();
        cols.put("race", "VARCHAR(255)");
        cols.put("world", "TEXT NOT NULL");
		cols.put("x", "INT NOT NULL");
		cols.put("y", "INT NOT NULL");
		cols.put("z", "INT NOT NULL");
		cols.put("yaw", "REAL");
        cols.put("PRIMARY KEY (race)", "");
        sqlHandler.createTable("persona_race_spawns", cols);*/
    }

    protected static void createBlockRegistryTable(Statement statement, String end) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS blockregistry (" +
                "date TIMESTAMP," +
                "world CHAR(36) NOT NULL," +
                "x INT," +
                "y INT," +
                "z INT," +
                "custom_data TEXT DEFAULT NULL," +
                "PRIMARY KEY (world,x,y,z)" +
                ")" +
                end);

        //TODO reimplement
        //statement.execute("DELETE FROM blockregistry WHERE ROWID IN (SELECT ROWID FROM blockregistry ORDER BY ROWID DESC LIMIT -1 OFFSET 5000);");
        //Blockregistry persistence stuff
        /*Map<String,String> 	cols = Maps.newLinkedHashMap();
        cols.put("world", "TEXT NOT NULL");
		cols.put("x", "INT");
		cols.put("y", "INT");
		cols.put("z", "INT");
		cols.put("PRIMARY KEY (world, x, y, z)", "");
		sqlHandler.createTable("blockregistry", cols);

		sqlHandler.execute("DELETE FROM blockregistry WHERE ROWID IN (SELECT ROWID FROM blockregistry ORDER BY ROWID DESC LIMIT -1 OFFSET 5000)");*/
    }

    protected static void createDeleteProcedure(SQLHandler handler) throws SQLException {
        //TODO fix this with the new table structure and log... more.
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

    private static String getEndingString(SQLHandler handler) {
        if (handler instanceof ArcheSQLiteHandler) {
            return ";";
        } else {
            return "ENGINE=InnoDB DEFAULT CHARSET=utf8;";
        }
    }
}
