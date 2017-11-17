package net.lordofthecraft.arche.save;

import java.sql.*;

import static net.lordofthecraft.arche.save.PersonaTable.*;
import static java.sql.JDBCType.*;

public enum PersonaField {
	PERSONA_ID("persona_id", MASTER, INTEGER, true),
	SLOT("slot", MASTER, INTEGER, true),
    PREFIX("prefix", MASTER, VARCHAR),
    NAME("name", MASTER, VARCHAR, true),
    RACE("race_header", MASTER, VARCHAR),
    RACE_REAL("race", MASTER, VARCHAR, true),
    DESCRIPTION("descr", MASTER, VARCHAR),
    SKILL_SELECTED("profession", MASTER, VARCHAR),
    CURRENT("curr", MASTER, BOOLEAN,true),
    MONEY("money", MASTER, DOUBLE),
    TYPE("p_type", MASTER, VARCHAR, true),
    GENDER("gender", MASTER, INTEGER, true),
    FATIGUE("fatigue", MASTER, DOUBLE),
    STAT_LAST_PLAYED("last_played", MASTER, TIMESTAMP, true),
	SKIN("skin_id_fk", MASTER, INTEGER),
	
    STAT_PLAYED("played", STATS, INTEGER),
    STAT_CHARS("chars", STATS, INTEGER),
    STAT_RENAMED("renamed", STATS, TIMESTAMP),
    STAT_CREATION("date_created", STATS, TIMESTAMP, true),
    STAT_PLAYTIME_PAST("playtime_past", STATS, INTEGER),

    WORLD("world", VITALS, VARCHAR, true),
    X("x", VITALS, INTEGER, true),
    Y("y", VITALS, INTEGER, true),
    Z("z", VITALS, INTEGER, true),
    INV("inv", VITALS, VARCHAR),
    ENDERINV("ender_inv", VITALS, VARCHAR),
    HEALTH("health", VITALS, DOUBLE),
    FOOD("hunger", VITALS, INTEGER),
    SATURATION("saturation", VITALS, FLOAT),
    POTIONS("potions", VITALS, VARCHAR);
	


    private final String field;
    public final PersonaTable table;
    public final SQLType type;
    private final boolean offline;
    
    PersonaField(String field, PersonaTable table, SQLType type) {
    	this(field, table, type, false);
    }
    
    PersonaField(String field, PersonaTable table, SQLType type, boolean offline) {
        this.field = field;
        this.table = table;
        this.type = type;
        this.offline = offline;
    }
    
    public boolean isForOfflinePersona() {
    	return offline;
    }
    
    public String field() { return this.field; }
}