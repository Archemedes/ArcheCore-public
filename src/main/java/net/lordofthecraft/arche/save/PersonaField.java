package net.lordofthecraft.arche.save;

import java.sql.*;

public enum PersonaField {
    PREFIX("prefix", PersonaTable.EXTRAS, JDBCType.VARCHAR),
    NAME("name", PersonaTable.EXTRAS, JDBCType.VARCHAR),
    RACE("rheader", PersonaTable.EXTRAS, JDBCType.VARCHAR),
    RACE_REAL("race", PersonaTable.MASTER, JDBCType.VARCHAR),
    DESCRIPTION("descr", PersonaTable.EXTRAS, JDBCType.VARCHAR),
    SKILL_SELECTED("skill_selected", PersonaTable.EXTRAS, JDBCType.VARCHAR),
    AGE("age", PersonaTable.EXTRAS, JDBCType.INTEGER),
    AUTOAGE("autoage", PersonaTable.EXTRAS, JDBCType.BOOLEAN),
    CURRENT("current", PersonaTable.EXTRAS, JDBCType.BOOLEAN),
    XP_GAIN("xpgain", PersonaTable.EXTRAS, JDBCType.BOOLEAN),
    STAT_PLAYED("stat_played", PersonaTable.STATS, JDBCType.INTEGER),
    STAT_CHARS("stat_chars", PersonaTable.STATS, JDBCType.INTEGER),
    STAT_RENAMED("stat_renamed", PersonaTable.STATS, JDBCType.TIMESTAMP),
    STAT_CREATION("stat_creation", PersonaTable.STATS, JDBCType.TIMESTAMP),
    STAT_PLAYTIME_PAST("stat_playtime_past", PersonaTable.STATS, JDBCType.INTEGER),
    MONEY("money", PersonaTable.EXTRAS, JDBCType.DOUBLE),
    ICON("skindata", PersonaTable.EXTRAS, JDBCType.VARCHAR),
    TYPE("p_type", PersonaTable.MASTER, JDBCType.VARCHAR),
    GENDER("gender", PersonaTable.EXTRAS, JDBCType.INTEGER);

    private static final String STATEMENT_PREFIX = "UPDATE persona SET ";
	private static final String STATEMENT_SUFFIX = " = ? WHERE player = ? AND id = ?";
	
	private final String field;
    private final PersonaTable table;
    public final SQLType type;
    private PreparedStatement stat = null;


    PersonaField(String field, PersonaTable table, SQLType type) {
        this.field = field;
        this.table = table;
        this.type = type;
    }

    public PreparedStatement getStatement(Connection c) throws SQLException {
        if (stat == null)
            stat = c.prepareStatement("UPDATE " + table.getTable() + " SET " + field + STATEMENT_SUFFIX + (table == PersonaTable.MASTER ? "=?" : "_fk=?"));
        return stat;
	}
}