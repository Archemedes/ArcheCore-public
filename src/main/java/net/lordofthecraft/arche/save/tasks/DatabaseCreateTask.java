package net.lordofthecraft.arche.save.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates Archecore's database from scratch if it is needed.
 *
 * @author 501warhead
 */
public class DatabaseCreateTask extends ArcheTask{

    public DatabaseCreateTask() {
    }

    @Override
    public void run() {
        Connection conn = handle.getConnection();
        try {
            Statement stat = conn.createStatement();
            stat.executeUpdate("CREATE TABLE IF NOT EXISTS players (" +
                    "player CHAR(36)," +
                    "preload_force BOOLEAN DEFAULT FALSE," +
                    "PRIMARY KEY (player)" +
                    ")" +
                    "ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE INDEX idx_player ON players (player);");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS sns_blacklist (" +
                    "item_type INT," +
                    "material VARCHAR(255)," +
                    "PRIMARY KEY (item_type,material)" +
                    ")" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS races (" +
                    "race_key VARCHAR(255)," +
                    "max_age INT UNSIGNED," +
                    "r_name TEXT," +
                    "base_xp_mult DOUBLE(10,2) DEFAULT 1.0," +
                    "luck_value DOUBLE(10,2) DEFAULT 0.0," +
                    "special         BOOLEAN DEFAULT FALSE," +
                    "parent_race VARCHAR(255) DEFAULT NULL," +
                    "PRIMARY KEY (race_key)" +
                    ")" +
                    "ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE INDEX idx_race_key ON races (race_key);");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS persona (" +
                    "            persona_id CHAR(36)," +
                    "    player_fk CHAR(36) NOT NULL," +
                    "    id INT UNSIGNED NOT NULL," +
                    "    race_key_fk VARCHAR(255) NOT NULL," +
                    "    gender ENUM('m','f','o') DEFAULT 'o'," +
                    "    p_type          TEXT DEFAULT 'NORMAL'," +
                    "    PRIMARY KEY (persona_id)," +
                    "    FOREIGN KEY (player_fk) REFERENCES players (player)," +
                    "    FOREIGN KEY (race_key_fk) REFERENCES races (race_key)" +
                    "            )" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS skills (" +
                    "            skill VARCHAR(255)," +
                    "    hidden INT DEFAULT 0," +
                    "    PRIMARY KEY (skill)" +
                    ")" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE INDEX idx_skill ON skills (skill);");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS bonus_exp_types (" +
                    "            exp_type VARCHAR(255)," +
                    "    PRIMARY KEY (exp_type)" +
                    ")" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE INDEX idx_exp_type ON bonus_exp_types (exp_type);");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS persona_log (" +
                    "            log_id INT UNSIGNED AUTO_INCREMENT," +
                    "            player_fk CHAR(36) NOT NULL," +
                    "    id TINYINT(1) NOT NULL," +
                    "    persona_id CHAR(36) NOT NULL," +
                    "    pers_name TEXT NOT NULL," +
                    "    pers_race TEXT," +
                    "    date_created TIMESTAMP NOT NULL," +
                    "    date_removed TIMESTAMP NOT NULL," +
                    "    stat_played INT UNSIGNED," +
                    "    stat_chars INT UNSIGNED," +
                    "    stat_renamed INT UNSIGNED," +
                    "    stat_playtime_past INT UNSIGNED," +
                    "    inv TEXT," +
                    "    money_personal DOUBLE(10,2)," +
                    "    money_bank DOUBLE (10,2)," +
                    "    PRIMARY KEY (log_id)," +
                    "    FOREIGN KEY (player_fk) REFERENCES players (player)" +
                    ")" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE INDEX idx_log_id ON persona_log (log_id);");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS persona_skills (" +
                    "            skill_id INT UNSIGNED AUTO_INCREMENT," +
                    "            skill_fk VARCHAR(255) NOT NULL," +
                    "    persona_id_fk CHAR(36) NOT NULL," +
                    "    skill_slot TINYINT(1) DEFAULT 3," +
                    "    xp DOUBLE(255,2) DEFAULT 0.00," +
                    "    visible BOOLEAN DEFAULT TRUE," +
                    "    PRIMARY KEY (skill_id)," +
                    "    FOREIGN KEY (skill_fk) REFERENCES skills (skill)," +
                    "    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id)," +
                    "    CONSTRAINT unique_skills UNIQUE (persona_id_fk, skill_fk)" +
                    ")" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE INDEX idx_skill_id ON persona_skills (skill_id);");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS race_spawns (" +
                    "            race_key_fk VARCHAR(255)," +
                    "    world VARCHAR(255) NOT NULL," +
                    "    x INT NOT NULL," +
                    "    y INT NOT NULL," +
                    "    z INT NOT NULL," +
                    "    PRIMARY KEY (race_key_fk)," +
                    "    FOREIGN KEY (race_key_fk) REFERENCES races (race_key) ON UPDATE CASCADE" +
                    ")" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS persona_extras (" +
                    "            persona_id_fk CHAR(36)," +
                    "    persona_name TEXT NOT NULL," +
                    "    curr BOOLEAN NOT NULL," +
                    "    rheader TEXT," +
                    "    autoage BOOLEAN," +
                    "    age INT UNSIGNED NOT NULL," +
                    "    xpgain BOOLEAN," +
                    "    descr TEXT," +
                    "    pref TEXT," +
                    "    money DOUBLE(10,2)," +
                    "    skindataTEXT," +
                    "    skill_fk        VARCHAR(255) DEFAULT NULL," +
                    "    PRIMARY KEY (persona_id_fk)," +
                    "    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE," +
                    "    FOREIGN KEY (skill_fk) REFERENCES skill (skill_id) ON UPDATE CASCADE" +
                    ")" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS persona_world (" +
                    "            persona_id_fk CHAR(36)," +
                    "    world VARCHAR(255)," +
                    "    x INT NOT NULL," +
                    "    y INT NOT NULL," +
                    "    z INT NOT NULL," +
                    "    inv TEXT," +
                    "    PRIMARY KEY (persona_id_fk)," +
                    "    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE" +
                    ")" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS persona_tags (" +
                    "            persona_id_fk CHAR(36) NOT NULL," +
                    "    tag_name TEXT NOT NULL," +
                    "    tag TEXT," +
                    "    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE CASCADE" +
                    ")" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS persona_stats (" +
                    "            persona_id_fk CHAR(36)," +
                    "    stat_played INT UNSIGNED," +
                    "    stat_chars INT UNSIGNED," +
                    "    stat_renamed TIMESTAMP," +
                    "    stat_playtime_past INT UNSIGNED," +
                    "    date_created TIMESTAMP NOT NULL," +
                    "    last_played TIMESTAMP," +
                    "    PRIMARY KEY (persona_id_fk)," +
                    "    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE" +
                    ")" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS accs (" +
                    "            persona_id_fk CHAR(36)," +
                    "    money DOUBLE(10,2)," +
                    "    PRIMARY KEY (persona_id_fk)," +
                    "    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id)" +
                    "            )" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("    CREATE TABLE IF NOT EXISTS bonus_exp_modifiers (" +
                    "            mod_id INT UNSIGNED AUTO_INCREMENT," +
                    "            xp_type_fk VARCHAR(255) NOT NULL," +
                    "    duration INT UNSIGNED," +
                    "    starttime TIMESTAMP NOT NULL," +
                    "    startxp DOUBLE(10,2) NOT NULL," +
                    "    capxp DOUBLE(10,2)," +
                    "    persona_id_fk CHAR(36) NOT NULL," +
                    "    skill_id_fk INT UNSIGNED," +
                    "    multiplier DOUBLE(10,2) NOT NULL," +
                    "    PRIMARY KEY (mod_id)," +
                    "    FOREIGN KEY (xp_type_fk) REFERENCES bonus_exp_types (exp_type)," +
                    "    FOREIGN KEY (skill_id_fk) REFERENCES persona_skills (skill_id) ON DELETE CASCADE," +
                    "    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON DELETE CASCADE" +
                    ")" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE INDEX idx_mod_id ON bonus_exp_modifiers (mod_id);");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS persona_skill_log (" +
                    "            log_id_fk INT UNSIGNED," +
                    "            skill VARCHAR(255)," +
                    "    xp DOUBLE(10,2) NOT NULL," +
                    "    visible BOOLEAN," +
                    "    selected BOOLEAN," +
                    "    slot TINYINT(1) NOT NULL DEFAULT 3," +
                    "    PRIMARY KEY (log_id_fk,skill)," +
                    "    FOREIGN KEY (log_id_fk) REFERENCES persona_log (log_id)" +
                    "            )" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS race_xp (" +
                    "            race_key_fk VARCHAR(255)," +
                    "    skill_fk VARCHAR(255)," +
                    "    xp_mult DOUBLE(10,2) DEFAULT 1.0," +
                    "    racial_skillBOOLEAN DEFAULT FALSE," +
                    "    PRIMARY KEY (race_key_fk, skill_fk)," +
                    "    FOREIGN KEY (race_key_fk) REFERENCES races (race_key)," +
                    "    FOREIGN KEY (skill_fk) REFERENCES skills (skill)" +
                    "            )" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS casket_log (" +
                    "            log_id INT UNSIGNED AUTO_INCREMENT," +
                    "            log_time TIMESTAMP DEFAULT NOW()," +
                    "    persona_id_fk CHAR(36) NOT NULL," +
                    "    luck DOUBLE(10,2) DEFAULT 1.0," +
                    "    rewards TEXT," +
                    "    PRIMARY KEY (log_id)," +
                    "    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id)" +
                    "            )" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS persona_names (" +
                    "            player_fk CHAR(36)," +
                    "    id TINYINT(1)," +
                    "    name TEXT," +
                    "    PRIMARY KEY (player_fk,id)," +
                    "    FOREIGN KEY (player_fk) REFERENCES players (player)" +
                    "            )" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS sktome_log (" +
                    "            log_id INT UNSIGNED AUTO_INCREMENT," +
                    "            log_time TIMESTAMP DEFAULT NOW()," +
                    "    persona_id_fk CHAR(36) NOT NULL," +
                    "    xp DOUBLE(10,2)," +
                    "    skill VARCHAR(255) NOT NULL," +
                    "    PRIMARY KEY (log_id)," +
                    "    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id)" +
                    "            )" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("CREATE TABLE IF NOT EXISTS blockregistry (" +
                    "            world VARCHAR(255)," +
                    "    x INT," +
                    "    y INT," +
                    "    z INT," +
                    "    PRIMARY KEY (world,x,y,z)" +
                    ")" +
                    "    ENGINE=InnoDB DEFAULT CHARSET=utf8;");

            stat.executeUpdate("DELIMITER $$" +
                    "    CREATE PROCEDURE delete_persona(" +
                    "            IN pl CHAR(36)," +
                    "    IN prid INT)" +
                    "    LANGUAGE SQL" +
                    "    DETERMINISTIC" +
                    "    COMMENT 'Logs and deletes persona entries based off of a UUID and a persona number.'" +
                    "    BEGIN" +
                    "    DECLARE pers_id CHAR(36);" +
                    "    DECLARE p_name TEXT;" +
                    "    DECLARE p_race TEXT;" +
                    "    DECLARE p_st_played INT UNSIGNED;" +
                    "    DECLARE p_st_char INT UNSIGNED;" +
                    "    DECLARE p_st_played_old INT UNSIGNED;" +
                    "    DECLARE p_st_renamed INT UNSIGNED;" +
                    "    DECLARE p_date_created TIMESTAMP;" +
                    "    DECLARE p_inv TEXT;" +
                    "    DECLARE mon_pers DOUBLE(10,2);" +
                    "    DECLARE mon_bank DOUBLE(10,2);" +
                    "" +
                    "    DECLARE done INT DEFAULT FALSE;" +
                    "    DECLARE new_log_id INT UNSIGNED;" +
                    "    DECLARE skill_name VARCHAR(255);" +
                    "    DECLARE skill_xp DOUBLE(10,2);" +
                    "    DECLARE skill_select BOOLEAN;" +
                    "    DECLARE skill_slot_num TINYINT(1);" +
                    "    DECLARE skill_visible BOOLEAN;" +
                    "" +
                    "    DECLARE skill_curs CURSOR FOR SELECT skill_fk,skill_selected,skill_slot,xp,visible FROM persona_skills WHERE persona_id_fk=pers_id;" +
                    "    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;" +
                    "" +
                    "    SELECT persona_id INTO pers_id FROM persona WHERE player_fk=pl AND id=prid;" +
                    "    SELECT persona_name INTO p_name FROM persona_extras WHERE persona_id_fk=pers_id;" +
                    "    SELECT race INTO p_race FROM persona WHERE persona_id=pers_id;" +
                    "    SELECT stat_played INTO p_st_played FROM persona_stats WHERE persona_id_fk=pers_id;" +
                    "    SELECT stat_chars INTO p_st_char FROM persona_stats WHERE persona_id_fk=pers_id;" +
                    "    SELECT stat_playtime_past INTO p_st_played_old FROM persona_stats WHERE persona_id_fk=pers_id;" +
                    "    SELECT stat_renamed INTO p_st_renamed FROM persona_stats WHERE persona_id_fk=pers_id;" +
                    "    SELECT persona_stats.date_created INTO p_date_created FROM persona_stats WHERE persona_id_fk=pers_id;" +
                    "    SELECT inv INTO p_inv FROM persona_world WHERE persona_id_fk=pers_id;" +
                    "    SELECT money INTO mon_pers FROM persona_extras WHERE persona_id_fk=pers_id;" +
                    "    SELECT accs.money INTO mon_bank FROM accs WHERE persona_id_fk=pers_id;" +
                    "" +
                    "    INSERT INTO persona_log (player,id,persona_id,pers_name,race,date_created,date_removed,stat_played,stat_chars,stat_renamed,stat_playtime_past,inv,money_personal,money_bank)" +
                    "    VALUES (pl, prid, pers_id, p_name, p_race, p_date_created, NOW(), p_st_played, p_st_char, p_st_renamed, p_st_played_old, p_inv, mon_pers, mon_bank);" +
                    "    SELECT DISTINCT log_id INTO new_log_id FROM persona_log WHERE player=pl AND id=prid ORDER BY date_removed;" +
                    "" +
                    "    OPEN skill_curs;" +
                    "" +
                    "    WHILE done = false DO" +
                    "    FETCH skill_curs INTO skill_name,skill_select,skill_slot_num,skill_xp,skill_visible;" +
                    "    INSERT IGNORE INTO persona_skill_log VALUES (new_log_id,skill_name,skill_xp,skill_visible,skill_select,skill_slot_num);" +
                    "    END WHILE;" +
                    "" +
                    "    CLOSE skill_curs;" +
                    "    DELETE FROM persona_stats WHERE persona_id_fk=pers_id;" +
                    "    DELETE FROM persona_world WHERE persona_id_fk=pers_id;" +
                    "    DELETE FROM persona_extras WHERE persona_id_fk=pers_id;" +
                    "    DELETE FROM persona_skills WHERE persona_id_fk=pers_id;" +
                    "    DELETE FROM persona_tags WHERE persona_id_fk=pers_id;" +
                    "    DELETE FROM accs WHERE persona_id_fk=pers_id;" +
                    "    DELETE FROM chests WHERE persona_id_fk=pers_id;" +
                    "" +
                    "    DELETE FROM persona WHERE persona_id=pers_id;" +
                    "    END $$" +
                    "    DELIMITER ;");

            stat.executeUpdate("    DELIMITER $$" +
                    "    CREATE PROCEDURE populate_persona (" +
                    "            IN pl CHAR(36)," +
                    "    IN prid TINYINT(1))" +
                    "    LANGUAGE SQL" +
                    "    DETERMINISTIC" +
                    "    COMMENT 'Populates all the necessary tables with information relevant to a new persona'" +
                    "    BEGIN" +
                    "    DECLARE pers_id CHAR(36);" +
                    "    DECLARE done INT DEFAULT FALSE;" +
                    "    DECLARE skill_name VARCHAR(255);" +
                    "    DECLARE skill_cur CURSOR FOR SELECT skill FROM skills;" +
                    "    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;" +
                    "" +
                    "    SELECT persona_id INTO pers_id FROM persona WHERE player_fk=pl AND id=prid;" +
                    "" +
                    "    INSERT IGNORE INTO accs VALUES (pers_id, 0.00);" +
                    "    INSERT IGNORE INTO persona_stats VALUES (pers_id, 0, 0, 0, 0, NOW(), NOW());" +
                    "" +
                    "    OPEN skill_cur;" +
                    "    WHILE done = FALSE DO" +
                    "    FETCH skill_cur INTO skill_name;" +
                    "    INSERT IGNORE INTO persona_skills (skill_fk,persona_id_fk,skill_selected,skill_slot,xp,visible) VALUES (skill_name, pers_id, FALSE, 3, 0.00, TRUE);" +
                    "    END WHILE;" +
                    "    CLOSE skill_cur;" +
                    "    END $$" +
                    "    DELIMITER ;");

            stat.executeUpdate("DELIMITER $$" +
                    "    CREATE PROCEDURE delete_race (" +
                    "            IN o_race VARCHAR(255))" +
                    "    LANGUAGE SQL" +
                    "    DETERMINISTIC" +
                    "    COMMENT 'Removes a race properly and sets the personas who populated that race to UNSET'" +
                    "    BEGIN" +
                    "    INSERT IGNORE INTO races VALUES ('UNSET', 999999, '', 1.0, 0.0, NULL);" +
                    "" +
                    "    UPDATE persona SET race_key_fk='UNSET' WHERE race_key_fk=o_race;" +
                    "    DELETE FROM race_spawns WHERE race_key_fk=o_race;" +
                    "    DELETE FROM race_xp WHERE race_key_fk=o_race;" +
                    "    DELETE FROM races WHERE race_key=o_race;" +
                    "    END $$" +
                    "    DELIMITER ;");


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    CREATE DATABASE IF NOT EXISTS archecore;

USE archecore;

DROP INDEX idx_player ON players;
DROP INDEX idx_race_key ON races;
DROP INDEX idx_skill ON skills;
DROP INDEX idx_exp_type ON bonus_xp_types;
DROP INDEX idx_log_id ON persona_log;
DROP INDEX idx_pillar_id ON ss_pillars;
DROP INDEX idx_chest_id ON chest;
DROP INDEX idx_mod_id ON bonus_xp_modifiers;
DROP INDEX idx_net_id ON fish_nets;
DROP INDEX idx_npc_id ON snsnpc;
DROP INDEX idx_pet_id ON skill_pets;
DROP INDEX idx_skill_id ON persona_skills;

    DROP TABLE IF EXISTS race_xp;
    DROP TABLE IF EXISTS bonus_xp_modifiers;
    DROP TABLE IF EXISTS ss_souls;
    DROP TABLE IF EXISTS race_spawns;
    DROP TABLE IF EXISTS persona_extras;
    DROP TABLE IF EXISTS persona_world;
    DROP TABLE IF EXISTS persona_stats;
    DROP TABLE IF EXISTS persona_tags;
    DROP TABLE IF EXISTS accs;
    DROP TABLE IF EXISTS chests;
    DROP TABLE IF EXISTS snsnpc;
    DROP TABLE IF EXISTS skill_pets;
    DROP TABLE IF EXISTS fish_nets;
    DROP TABLE IF EXISTS buddylist;
    DROP TABLE IF EXISTS ss_homes;
    DROP TABLE IF EXISTS nexus_rods;
    DROP TABLE IF EXISTS books;
    DROP TABLE IF EXISTS blockregistry;
    DROP TABLE IF EXISTS atp_tents;
    DROP TABLE IF EXISTS auct;
    DROP TABLE IF EXISTS bank;
    DROP TABLE IF EXISTS atp_tents;
    DROP TABLE IF EXISTS ag_compost;
    DROP TABLE IF EXISTS nexus_log;
    DROP TABLE IF EXISTS casket_log;
    DROP TABLE IF EXISTS trans;
    DROP TABLE IF EXISTS trans_bank;
    DROP TABLE IF EXISTS persona_names;
    DROP TABLE IF EXISTS sktome_log;
    DROP TABLE IF EXISTS persona_log;
    DROP TABLE IF EXISTS persona_skill_log;
    DROP TABLE IF EXISTS persona_skills;
    DROP TABLE IF EXISTS persona;
    DROP TABLE IF EXISTS ss_pillars;
    DROP TABLE IF EXISTS players;
    DROP TABLE IF EXISTS races;
    DROP TABLE IF EXISTS skills;
    DROP TABLE IF EXISTS bonus_xp_types;
    DROP TABLE IF EXISTS item;
    DROP TABLE IF EXISTS worlds;
    DROP TABLE IF EXISTS price;

    DROP PROCEDURE IF EXISTS delete_persona;
    DROP PROCEDURE IF EXISTS populate_persona;
    DROP PROCEDURE IF EXISTS delete_race;

    CREATE TABLE IF NOT EXISTS books (
            book_id INT UNSIGNED AUTO_INCREMENT,
            world VARCHAR(255) NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    z INT NOT NULL,
    slot INT,
    mat TEXT,
    book_count INT,
    damage INT,
    title TEXT,
    author TEXT,
    lore TEXT,
    pages TEXT,
    PRIMARY KEY (book_id)
)
    ENGINE=InnoDB DEFAULT CHARSET=utf8;

    CREATE TABLE IF NOT EXISTS atp_tents (
            world VARCHAR(255),
    x INT,
    y INT,
    z INT,
    colone TEXT,
    coltwo TEXT,
    tent_type INT,
    orientation INT DEFAULT 0,
    PRIMARY KEY (world,x,y,z)
)
    ENGINE=InnoDB DEFAULT CHARSET=utf8;

    CREATE TABLE IF NOT EXISTS ag_compost (
            world VARCHAR(255),
    x INT,
    y INT,
    z INT,
    total DOUBLE(10,2),
    speed DOUBLE(10,2),
    stage INT,
    running INT,
    comp_start INT,
    PRIMARY KEY (world,x,y,z)
)
    ENGINE=InnoDB DEFAULT CHARSET=utf8;
     */
}
