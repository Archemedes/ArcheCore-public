package net.lordofthecraft.arche.save;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public enum PersonaField {
	PREFIX("prefix"),
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
	SKIN("skin");
	
	private static final String STATEMENT_PREFIX = "UPDATE persona SET ";
	private static final String STATEMENT_SUFFIX = " = ? WHERE player = ? AND id = ?";
	
	private final String field;
	private PreparedStatement stat = null;

	PersonaField(String field) {
		this.field = field;
	}
	
	public PreparedStatement getStatement(Connection c) throws SQLException{
		if(stat == null) stat = c.prepareStatement(STATEMENT_PREFIX + field + STATEMENT_SUFFIX);
		return stat;
	}
}
