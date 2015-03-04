package net.lordofthecraft.arche.save;

import java.sql.*;

public enum PersonaField
{
    PREFIX("prefix"), 
    NAME("name"), 
    RACE("rheader"), 
    DESCRIPTION("desc"), 
    SKILL_SELECTED("skill_selected"), 
    AGE("age"), 
    AUTOAGE("autoage"), 
    CURRENT("current"), 
    XP_GAIN("skill_xpgain"), 
    STAT_PLAYED("stat_played"), 
    STAT_CHARS("stat_chars"), 
    STAT_RENAMED("stat_renamed"), 
    SKILL_PRIMARY(""), 
    SKILL_SECONDARY(""), 
    SKILL_ADDITIONAL(""), 
    MONEY("money");
    
    private static final String STATEMENT_PREFIX = "UPDATE persona SET ";
    private static final String STATEMENT_SUFFIX = " = ? WHERE player = ? AND id = ?";
    private final String field;
    private PreparedStatement stat;
    
    private PersonaField(final String field) {
        this.stat = null;
        this.field = field;
    }
    
    public PreparedStatement getStatement(final Connection c) throws SQLException {
        if (this.stat == null) {
            this.stat = c.prepareStatement("UPDATE persona SET " + this.field + " = ? WHERE player = ? AND id = ?");
        }
        return this.stat;
    }
}
