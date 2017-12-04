package net.lordofthecraft.arche.save.rows;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.util.SQLUtil;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public abstract class StatementRow implements ArcheRow {
    protected static Set<PreparedStatement> statPool = identityHashSet();

    private String briefStackTrace = null;
    
    public static void close() { //Called by consumer
        statPool.forEach(t -> {
            try {
                t.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        statPool = identityHashSet();
    }

    public StatementRow() {
    	if(ArcheCore.getConsumerControls().isDebugging()) {
    		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    		int i = 0;
    		while(!stackTrace[i].getClassName().equals(this.getClass().getName())) i++;
    		
    		briefStackTrace = stackTrace[i+1].toString() + '\n' + stackTrace[i+2].toString();
    	}
    }
    
    public String getOriginStackTrace() {
    	return briefStackTrace;
    }
    
    public abstract boolean isUnique();

    public abstract PreparedStatement[] prepare(Connection connection) throws SQLException;

    @Override
    public final String[] getInserts() { //Works in most cases else override
        String sql[] = getStatements();
        String finalResult[] = new String[sql.length];

        for (int h = 0; h < sql.length; h++) {
            String[] bits = sql[h].split("\\?");
            StringBuilder result = new StringBuilder();
            for (int i = 1; i <= bits.length; i++) {
                result.append(bits[i - 1]);

                Object o = getValueFor(h, i);
                if (o instanceof Number || o instanceof Boolean || o instanceof Timestamp) {
                    result.append(o.toString());
                } else { //String, UUID, enum
                    result.append('\'').append(SQLUtil.mysqlTextEscape(o.toString())).append('\'');
                }
            }

            finalResult[h] = result.toString();
        }

        return finalResult;
    }

    public final void setValues(PreparedStatement[] stats) throws SQLException {
        String[] sqls = getStatements();

        for (int h = 0; h < sqls.length; h++) {
            String sql = sqls[h];
            PreparedStatement statement = stats[h];
            int amountOfVariables = StringUtils.countMatches(sql, "?");
            for (int i = 1; i <= amountOfVariables; i++) {
                Object o = getValueFor(h + 1, i);

                if (o instanceof Number) { //Numbers in order of decreasing likelihood
                    if (o instanceof Integer) {
                        statement.setInt(i, (Integer) o);
                    } else if (o instanceof Double) {
                        statement.setDouble(i, (Double) o);
                    } else if (o instanceof Long) {
                        statement.setLong(i, (Long) o);
                    } else if (o instanceof Float) {
                        statement.setFloat(i, (Float) o);
                    } else if (o instanceof Short) {
                        statement.setShort(i, (Short) o);
                    } else if (o instanceof Byte) {
                        statement.setByte(i, (Byte) o);
                    } else {
                        statement.setInt(i, ((Number) o).intValue());
                        ArcheCore.getPlugin().getLogger().warning("unhandled Number implementation being used: " + o.getClass().getName() + ". Int assumed");
                    }
                } else if (o instanceof Timestamp) {
                    statement.setTimestamp(i, (Timestamp) o);
                } else if (o instanceof Boolean) {
                    statement.setBoolean(i, (Boolean) o);
                } else { //String, enums, uuid
                    statement.setString(i, o == null ? null : o.toString());
                }
            }
        }
    }

    protected abstract String[] getStatements();

    protected abstract Object getValueFor(int statement, int varIndex);

    public static boolean usingSQLite() {
        return ArcheCore.getControls().isUsingSQLite();
    }

    public static String orIgnore() {
        return usingSQLite() ? "IGNORE" : "OR IGNORE";
    }

    public static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    protected static Set<PreparedStatement> identityHashSet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }
}
