CREATE DATABASE IF NOT EXISTS archecore;

USE archecore;

/* parent */
/* This is a players tables and its intention is to perform referential integrity. */
CREATE TABLE IF NOT EXISTS players (
    player 		    CHAR(36),
    force_preload   BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (player)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS persona_skins (
    skin_id         INT AUTO_INCREMENT,
    player          CHAR(36) NOT NULL,
    slot            INT,
    name            TEXT,
    skinUrl         TEXT,
    slim            BOOLEAN DEFAULT FALSE,
    skinValue       TEXT,
    skinSignature   TEXT,
    refresh         TIMESTAMP,
    PRIMARY KEY (skin_id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* parent */
CREATE TABLE IF NOT EXISTS skills (
    skill_id    VARCHAR(255),
    hidden 		INT DEFAULT 0,
    help_text   TEXT,
    help_icon   TEXT,
    inert       BOOLEAN DEFAULT FALSE,
    male_name   TEXT,
    female_name TEXT,
    PRIMARY KEY (skill_id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Parent & Child */
CREATE TABLE IF NOT EXISTS persona (
    persona_id 		INT UNSIGNED AUTO_INCREMENT,
    player_fk 		CHAR(36) NOT NULL,
    slot 			INT UNSIGNED NOT NULL,
    race_key 	    VARCHAR(255) NOT NULL,
    name            TEXT,
    race_header     TEXT DEFAULT NULL,
    gender 			TEXT DEFAULT 'Other',
    p_type          TEXT DEFAULT 'NORMAL',
    descr           TEXT DEFAULT NULL,
    prefix          TEXT DEFAULT NULL,
    curr            BOOLEAN DEFAULT FALSE,
    money           DOUBLE DEFAULT 0.0,
    skin            INT DEFAULT -1,
    profession      VARCHAR(255) DEFAULT NULL,
    fatigue         DOUBLE DEFAULT 0.0,
    max_fatigue     DOUBLE DEFAULT 100.0,
    PRIMARY KEY (persona_id),
    FOREIGN KEY (player_fk) REFERENCES players (player) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY (profession) REFERENCES skills (skill_id) ON UPDATE CASCADE ON DELETE SET NULL,
    FOREIGN KEY (skin) REFERENCES persona_skins (skin_id) ON UPDATE CASCADE ON DELETE SET NULL

)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Child */
CREATE TABLE IF NOT EXISTS persona_vitals (
    persona_id_fk 	INT UNSIGNED,
    world 			VARCHAR(255),
    x 				INT NOT NULL,
    y 				INT NOT NULL,
    z 				INT NOT NULL,
    inv 			TEXT DEFAULT NULL,
    ender_inv       TEXT DEFAULT NULL,
    health          DOUBLE DEFAULT 10.0,
    hunger          INT DEFAULT 0,
    saturation      INT DEFAULT 0,
    PRIMARY KEY (persona_id_fk),
    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE,
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Child */
CREATE TABLE IF NOT EXISTS persona_tags (
    persona_id_fk 	INT UNSIGNED NOT NULL,
    tag_key     	TEXT NOT NULL,
    tag_value 		TEXT,
    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE ON DELETE CASCADE
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Child */
CREATE TABLE IF NOT EXISTS persona_stats (
    persona_id_fk 		INT UNSIGNED,
    played       		INT UNSIGNED DEFAULT 0,
    chars 	    		INT UNSIGNED DEFAULT 0,
    renamed      		TIMESTAMP DEFAULT NOW(),
    playtime_past    	INT UNSIGNED DEFAULT 0,
    date_created 		TIMESTAMP NOT NULL DEFAULT NOW(),
    last_played 		TIMESTAMP DEFAULT NOW(),
    PRIMARY KEY (persona_id_fk),
    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS skill_races (
    skill_id_fk     VARCHAR(255),
    race            VARCHAR(255) NOT NULL,
    racial_skill    BOOLEAN DEFAULT FALSE,
    racial_mod      DOUBLE DEFAULT 1.0,
    PRIMARY KEY (skill_id_fk,race),
    FOREIGN KEY (skill_id_fk) REFERENCES skills (skill_id) ON UPDATE CASCADE
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Junction */
CREATE TABLE IF NOT EXISTS persona_skills (
    persona_id_fk       INT UNSIGNED,
    skill_id_fk         VARCHAR(255),
    xp                  DOUBLE DEFAULT 0.0,
    visible             BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (persona_id_fk,skill_id_fk),
    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id) ON UPDATE CASCADE,
    FOREIGN KEY (skill_id_fk) REFERENCES skills (skill_id) ON UPDATE CASCADE
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* parent & child */
CREATE TABLE IF NOT EXISTS persona_log (
    log_id INT UNSIGNED AUTO_INCREMENT,
    player_fk CHAR(36) NOT NULL,
    id TINYINT(1) NOT NULL,
    persona_id CHAR(36) NOT NULL,
    pers_name TEXT NOT NULL,
    pers_race TEXT,
    date_created TIMESTAMP NOT NULL,
    date_removed TIMESTAMP NOT NULL,
    stat_played INT UNSIGNED,
    stat_chars INT UNSIGNED,
    stat_renamed INT UNSIGNED,
    stat_playtime_past INT UNSIGNED,
    inv TEXT,
    money_personal DOUBLE(10,2),
    money_bank DOUBLE (10,2),
    PRIMARY KEY (log_id),
    FOREIGN KEY (player_fk) REFERENCES players (player)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Child */
CREATE TABLE IF NOT EXISTS race_spawns (
    race     	    VARCHAR(255),
    world 			VARCHAR(255) NOT NULL,
    x 				INT NOT NULL,
    y 				INT NOT NULL,
    z 				INT NOT NULL,
    PRIMARY KEY (race),
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* Child */
CREATE TABLE IF NOT EXISTS persona_skill_log (
    log_id_fk INT UNSIGNED,
    skill VARCHAR(255),
    xp DOUBLE(10,2) NOT NULL,
    visible BOOLEAN,
    selected BOOLEAN,
    slot TINYINT(1) NOT NULL DEFAULT 3,
    PRIMARY KEY (log_id_fk,skill),
    FOREIGN KEY (log_id_fk) REFERENCES persona_log (log_id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

/* LOGGING */
CREATE TABLE IF NOT EXISTS casket_log (
    log_id INT UNSIGNED AUTO_INCREMENT,
    log_time TIMESTAMP DEFAULT NOW(),
    persona_id_fk CHAR(36) NOT NULL,
    luck DOUBLE(10,2) DEFAULT 1.0,
    rewards TEXT,
    PRIMARY KEY (log_id),
    FOREIGN KEY (persona_id_fk) REFERENCES persona (persona_id)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS persona_names (
    player_fk CHAR(36),
    id TINYINT(1),
    name TEXT,
    PRIMARY KEY (player_fk,id),
    FOREIGN KEY (player_fk) REFERENCES players (player)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS blockregistry (
    world VARCHAR(255),
    x INT,
    y INT,
    z INT,
    PRIMARY KEY (world,x,y,z)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8;

DELIMITER $$
CREATE PROCEDURE delete_persona(
IN pl CHAR(36),
IN prid INT)
LANGUAGE SQL
DETERMINISTIC
COMMENT 'Logs and deletes persona entries based off of a UUID and a persona number.'
BEGIN
	DECLARE pers_id CHAR(36);
	DECLARE p_name TEXT;
	DECLARE p_race TEXT;
	DECLARE p_st_played INT UNSIGNED;
	DECLARE p_st_char INT UNSIGNED;
	DECLARE p_st_played_old INT UNSIGNED;
	DECLARE p_st_renamed INT UNSIGNED;
	DECLARE p_date_created TIMESTAMP;
	DECLARE p_inv TEXT;
	DECLARE mon_pers DOUBLE(10,2);
	
	DECLARE done INT DEFAULT FALSE;
	DECLARE new_log_id INT UNSIGNED;
	
	DECLARE skill_curs CURSOR FOR SELECT skill_fk,skill_selected,skill_slot,xp,visible FROM persona_skills WHERE persona_id_fk=pers_id;
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
	
	SELECT persona_id INTO pers_id FROM persona WHERE player_fk=pl AND id=prid;
	SELECT persona_name INTO p_name FROM persona_extras WHERE persona_id_fk=pers_id;
	SELECT race INTO p_race FROM persona WHERE persona_id=pers_id;
	SELECT stat_played INTO p_st_played FROM persona_stats WHERE persona_id_fk=pers_id;
	SELECT stat_chars INTO p_st_char FROM persona_stats WHERE persona_id_fk=pers_id;
	SELECT stat_playtime_past INTO p_st_played_old FROM persona_stats WHERE persona_id_fk=pers_id;
	SELECT stat_renamed INTO p_st_renamed FROM persona_stats WHERE persona_id_fk=pers_id;
	SELECT persona_stats.date_created INTO p_date_created FROM persona_stats WHERE persona_id_fk=pers_id;
	SELECT inv INTO p_inv FROM persona_world WHERE persona_id_fk=pers_id;
	SELECT money INTO mon_pers FROM persona_extras WHERE persona_id_fk=pers_id;
	
	INSERT INTO persona_log (player,id,persona_id,pers_name,race,date_created,date_removed,stat_played,stat_chars,stat_renamed,stat_playtime_past,inv,money_personal,money_bank) 
	VALUES (pl, prid, pers_id, p_name, p_race, p_date_created, NOW(), p_st_played, p_st_char, p_st_renamed, p_st_played_old, p_inv, mon_pers);
	SELECT DISTINCT log_id INTO new_log_id FROM persona_log WHERE player=pl AND id=prid ORDER BY date_removed;
	
	OPEN skill_curs;
	
	WHILE done = false DO
		FETCH skill_curs INTO skill_name,skill_select,skill_slot_num,skill_xp,skill_visible;
		INSERT IGNORE INTO persona_skill_log VALUES (new_log_id,skill_name,skill_xp,skill_visible,skill_select,skill_slot_num);
	END WHILE;
	
	CLOSE skill_curs;
	DELETE FROM persona_stats WHERE persona_id_fk=pers_id;
	DELETE FROM persona_world WHERE persona_id_fk=pers_id;
	DELETE FROM persona_extras WHERE persona_id_fk=pers_id;
	DELETE FROM persona_skills WHERE persona_id_fk=pers_id;
	DELETE FROM persona_tags WHERE persona_id_fk=pers_id;
	
	DELETE FROM persona WHERE persona_id=pers_id;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE populate_persona (
IN pl CHAR(36),
IN prid TINYINT(1))
LANGUAGE SQL
DETERMINISTIC
COMMENT 'Populates all the necessary tables with information relevant to a new persona'
BEGIN
	DECLARE pers_id CHAR(36);
	DECLARE done INT DEFAULT FALSE;
	DECLARE skill_name VARCHAR(255);
	DECLARE skill_cur CURSOR FOR SELECT skill FROM skills;
	DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
	
	SELECT persona_id INTO pers_id FROM persona WHERE player_fk=pl AND id=prid;

	INSERT IGNORE INTO persona_stats VALUES (pers_id, 0, 0, 0, 0, NOW(), NOW());
	
	OPEN skill_cur;
	WHILE done = FALSE DO 
		FETCH skill_cur INTO skill_name;
		INSERT IGNORE INTO persona_skills (skill_fk,persona_id_fk,skill_selected,skill_slot,xp,visible) VALUES (skill_name, pers_id, FALSE, 3, 0.00, TRUE);
	END WHILE;
	CLOSE skill_cur;
END $$
DELIMITER ;

DELIMITER $$
CREATE PROCEDURE delete_race (
IN o_race VARCHAR(255))
LANGUAGE SQL
DETERMINISTIC
COMMENT 'Removes a race properly and sets the personas who populated that race to UNSET'
BEGIN
	INSERT IGNORE INTO races VALUES ('UNSET', 999999, '', 1.0, 0.0, NULL);
	
	UPDATE persona SET race_key_fk='UNSET' WHERE race_key_fk=o_race;
	DELETE FROM race_spawns WHERE race_key_fk=o_race;
	DELETE FROM race_xp WHERE race_key_fk=o_race;
	DELETE FROM races WHERE race_key=o_race;
END $$
DELIMITER ;