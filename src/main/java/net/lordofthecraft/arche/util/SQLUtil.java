package net.lordofthecraft.arche.util;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import net.lordofthecraft.arche.SQL.Syntax;

public class SQLUtil {

    protected SQLUtil() {
    }

    public static class ExtensionFilenameFilter implements FilenameFilter {
        private final String ext;

        public ExtensionFilenameFilter(String ext) {
            this.ext = ext;
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(ext);
        }
    }

    public static String mysqlTextEscape(String untrusted) {
        if (untrusted == null) return null;
        return untrusted.replace("\\", "\\\\").replace("'", "\\'");
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
            result.append(entry.getKey()).append('=');
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
            result.append(entry.getKey()).append('=');
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
                rs.getStatement().getConnection().close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(ResultSet closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Statement closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Connection closable) {
        if (closable != null) {
            try {
                closable.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
