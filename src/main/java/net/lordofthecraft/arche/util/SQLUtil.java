package net.lordofthecraft.arche.util;

import java.io.File;
import java.io.FilenameFilter;

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
        return untrusted.replace("\\", "\\\\").replace("'", "\\'");
    }
}
