package net.lordofthecraft.arche.save;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public enum PersonaField {
	//TODO: make sure this is properly cleaned up and reflects the new table structure
	/*PREFIX("prefix"),
	NAME("name"),
	RACE("rheader"),
	RACE_REAL("race"),
	DESCRIPTION("desc"),
	SKILL_SELECTED("skill_selected"),
	AGE("age"),
	AUTOAGE("autoage"),
	CURRENT("current"),
	XP_GAIN("skill_xpgain"),
	STAT_PLAYED("stat_played"),
	STAT_CHARS("stat_chars"),
	STAT_RENAMED("stat_renamed"),
	SKILL_PRIMARY("skill_primary"),
	SKILL_SECONDARY("skill_secondary"),
	SKILL_ADDITIONAL("skill_tertiary"),
	STAT_CREATION("stat_creation"),
	STAT_PLAYTIME_PAST("stat_playtime_past"),
	MONEY("money"),
	ICON("skindata"),
	GENDER("gender");*/
    PREFIX("prefix", PersonaTable.EXTRAS, false),
    NAME("name", PersonaTable.EXTRAS, false),
    RACE("rheader", PersonaTable.EXTRAS, false),
    RACE_REAL("race", PersonaTable.MASTER, false),
    DESCRIPTION("descr", PersonaTable.EXTRAS, false),
    SKILL_SELECTED("skill_selected", PersonaTable.EXTRAS, false),
    AGE("age", PersonaTable.EXTRAS, false),
    AUTOAGE("autoage", PersonaTable.EXTRAS, false),
    CURRENT("current", PersonaTable.EXTRAS, false),
    XP_GAIN("xpgain", PersonaTable.EXTRAS, false),
    STAT_PLAYED("stat_played", PersonaTable.STATS, false),
    STAT_CHARS("stat_chars", PersonaTable.STATS, false),
    STAT_RENAMED("stat_renamed", PersonaTable.STATS, true),
    //SKILL_PRIMARY("skill_primary", PersonaTable.SKILLS),
    //SKILL_SECONDARY("skill_secondary", PersonaTable.SKILLS),
    //SKILL_ADDITIONAL("skill_tertiary", PersonaTable.SKILLS),
    //SKILL_UNSELECTED("skill_unselected", PersonaTable.SKILLS),
    STAT_CREATION("stat_creation", PersonaTable.STATS, true),
    STAT_PLAYTIME_PAST("stat_playtime_past", PersonaTable.STATS, false),
    MONEY("money", PersonaTable.EXTRAS, false),
    ICON("skindata", PersonaTable.EXTRAS, false),
    TYPE("p_type", PersonaTable.MASTER, false),
    GENDER("gender", PersonaTable.EXTRAS, false);
	
	private static final String STATEMENT_PREFIX = "UPDATE persona SET ";
	private static final String STATEMENT_SUFFIX = " = ? WHERE player = ? AND id = ?";
	
	private final String field;
	private final PersonaTable table;
	public final boolean timestamp;
	private PreparedStatement stat = null;

	PersonaField(String field, PersonaTable table, boolean timestamp) {
		this.field = field;
		this.table = table;
		this.timestamp = timestamp;
	}

	public PreparedStatement getStatement(Connection c) throws SQLException{
		if(stat == null) stat = c.prepareStatement("UPDATE " + table.getTable() + " SET " + field + STATEMENT_SUFFIX + (table == PersonaTable.MASTER ? "=?" : "_fk=?"));
		return stat;
	}
}