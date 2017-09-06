package net.lordofthecraft.arche.save;

import java.sql.*;

public enum PersonaField {
    PREFIX("prefix", PersonaTable.VITALS, JDBCType.VARCHAR),
    NAME("name", PersonaTable.MASTER, JDBCType.VARCHAR),
    RACE("race_header", PersonaTable.MASTER, JDBCType.VARCHAR),
    RACE_REAL("race", PersonaTable.MASTER, JDBCType.VARCHAR),
    DESCRIPTION("descr", PersonaTable.MASTER, JDBCType.VARCHAR),
    SKILL_SELECTED("profession", PersonaTable.MASTER, JDBCType.VARCHAR),
    CURRENT("curr", PersonaTable.MASTER, JDBCType.BOOLEAN),
    STAT_PLAYED("played", PersonaTable.STATS, JDBCType.INTEGER),
    STAT_CHARS("chars", PersonaTable.STATS, JDBCType.INTEGER),
    STAT_RENAMED("renamed", PersonaTable.STATS, JDBCType.TIMESTAMP),
    STAT_CREATION("date_created", PersonaTable.STATS, JDBCType.TIMESTAMP),
    STAT_PLAYTIME_PAST("playtime_past", PersonaTable.STATS, JDBCType.INTEGER),
    MONEY("money", PersonaTable.MASTER, JDBCType.DOUBLE),
    ICON("skin", PersonaTable.MASTER, JDBCType.VARCHAR),
    TYPE("p_type", PersonaTable.MASTER, JDBCType.VARCHAR),
    GENDER("gender", PersonaTable.MASTER, JDBCType.INTEGER),
    FATIGUE("fatigue", PersonaTable.MASTER, JDBCType.DOUBLE),
    MAX_FATIGUE("max_fatigue", PersonaTable.MASTER, JDBCType.DOUBLE),
    WORLD("world", PersonaTable.VITALS, JDBCType.VARCHAR),
    X("x", PersonaTable.VITALS, JDBCType.INTEGER),
    Y("y", PersonaTable.VITALS, JDBCType.INTEGER),
    Z("z", PersonaTable.VITALS, JDBCType.INTEGER),
    INV("inv", PersonaTable.VITALS, JDBCType.VARCHAR),
    ENDERINV("ender_inv", PersonaTable.VITALS, JDBCType.VARCHAR),
    HEALTH("health", PersonaTable.VITALS, JDBCType.DOUBLE),
    FOOD("food", PersonaTable.VITALS, JDBCType.INTEGER),
    SATURATION("saturation", PersonaTable.VITALS, JDBCType.FLOAT);

    private static final String STATEMENT_SUFFIX = " = ? WHERE persona_id";

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