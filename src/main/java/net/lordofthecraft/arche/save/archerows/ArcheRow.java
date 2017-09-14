package net.lordofthecraft.arche.save.archerows;

import net.lordofthecraft.arche.ArcheCore;
import net.lordofthecraft.arche.save.Consumer;

/**
 * Represents the most basic SQL task for ArcheCore. Inserts will be run as-is assuming that the declaration is not of a {@link net.lordofthecraft.arche.save.archerows.ArchePreparedStatementRow} instead
 *
 * @author 501warhead
 */
public interface ArcheRow {

    /**
     * Get the SQL commands for this particular row.
     * <p>
     * Note, if your row is a {@link ArchePreparedStatementRow} or another subclass of it these will only be used in one circumstance
     * which is if the {@link net.lordofthecraft.arche.save.Consumer} fails to shut down then it will be called in {@link Consumer#writeToFile()} to write to an <i>.sql</i> file
     * <p>
     * They will then be run in {@link ArcheCore#onEnable()} using {@link net.lordofthecraft.arche.save.DumpedDBReader} on next run.
     * <p>
     * For the most part these will be the exact same SQL commands you might use in {@link ArchePreparedStatementRow#executeStatements()} with string concatenation instead of {@link java.sql.PreparedStatement#setObject(int, Object)}s
     * <p>
     * In the event of the data being "User Generated" (e.g. {@link net.lordofthecraft.arche.persona.ArchePersona#setApparentRace(String)})
     * use {@link net.lordofthecraft.arche.util.SQLUtil#mysqlTextEscape(String)} to lower possibility of SQL injection.
     * <p>
     * @return The SQL commands for this particular entry or update
     */
    String[] getInserts();
}
