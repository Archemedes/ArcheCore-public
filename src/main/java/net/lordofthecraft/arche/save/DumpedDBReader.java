package net.lordofthecraft.arche.save;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.util.SQLUtil;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class DumpedDBReader implements Runnable {

    private final ArcheCore ac;

    public DumpedDBReader(ArcheCore ac) {
        this.ac = ac;
    }

    @Override
    public void run() {
        final File[] imports = new File("plugins/ArcheCore/import/").listFiles(new SQLUtil.ExtensionFilenameFilter("sql"));
        if (imports != null && imports.length > 0) {
            ac.getLogger().info("We found dumped database entries from a botched database save. Proceeding to import " + imports.length + " files.");
            Connection connection = null;
            try {
                connection = ac.getSQLHandler().getConnection();
                if (connection == null) {
                    return;
                }
                connection.setAutoCommit(false);
                final Statement st = connection.createStatement();
                final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(ac.getDataFolder(), "import/failedimports.txt")));
                int success = 0;
                int errors = 0;
                for (final File sqlFile : imports) {
                    ac.getLogger().info("Trying to import " + sqlFile.getName() + " now...");
                    final BufferedReader read = new BufferedReader(new FileReader(sqlFile));
                    String sqlline;
                    while ((sqlline = read.readLine()) != null) {
                        try {
                            st.execute(sqlline);
                            success++;
                        } catch (final Exception ex) {
                            ac.getLogger().log(Level.WARNING, "Error while importing: '" + sqlline + "': ", ex);
                            writer.write(sqlline + "\n");
                            errors++;
                        }
                    }
                    connection.commit();
                    read.close();
                    sqlFile.delete();
                    ac.getLogger().info("Successfully imported " + sqlFile.getName() + " into the AC Database.");
                }
                writer.close();
                st.close();
                ac.getLogger().info("Successfully imported the dumped queue. (" + success + " changes successfully imported with " + errors + " errors logged in /plugins/ArcheCore/import/failedimports.txt)");
            } catch (Exception ex) {
                ac.getLogger().log(Level.WARNING, "Error while importing the dumped data! This isn't good but the rows should still be in file form.", ex);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException ex) {
                        ac.getLogger().log(Level.WARNING, "Failed to close out the Connection for DumpedDBReader!", ex);
                    }
                }
            }
        }
    }
}
