package net.lordofthecraft.arche.SQL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * A Util file for formatting SQL statements
 *
 * @author 501warhead (Why)
 */
public class SQLUtils {
    private SQLUtils() {
    }

    public static String giveOptionalWhere(Map<String, Object> criteria) {
        return (criteria == null ? "" : " WHERE " + formatWhereClause(criteria));
    }

    public static String formatWhereClause(Map<String, Object> val) {
        String div = "";
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Object> entry : val.entrySet()) {
            result.append(div);
            div = " AND ";
            result.append(entry.getKey() + '=');
            Object o = entry.getValue();
            if (o == null) o = new Syntax("NULL");
            else if (o instanceof Boolean) o = ((Boolean) o) ? 1 : 0;
            else if (o instanceof String) o = ((String) o).replace(';', ' ').replace("'", "''");
            boolean noQuotes = (o instanceof Number) || (o instanceof Syntax);
            String condition = noQuotes ? o.toString() : "'" + o.toString() + "'";
            result.append(condition);
        }

        return result.toString();
    }

    public static String formatSetClause(Map<String, Object> val) {
        String div = "";
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Object> entry : val.entrySet()) {
            result.append(div);
            div = ",";
            result.append(entry.getKey() + '=');
            Object o = entry.getValue();
            if (o == null) o = new Syntax("NULL");
            else if (o instanceof Boolean) o = ((Boolean) o) ? 1 : 0;
            else if (o instanceof String)
                o = ((String) o).replace('(', ' ').replace(')', ' ').replace(';', ' ').replace("'", "''");
            boolean noQuotes = (o instanceof Number) || (o instanceof Syntax);
            String condition = noQuotes ? o.toString() : "'" + o.toString() + "'";
            result.append(condition);
        }

        return result.toString();
    }

    public static void closeStatement(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                rs.getStatement().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
