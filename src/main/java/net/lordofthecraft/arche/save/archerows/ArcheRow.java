package net.lordofthecraft.arche.save.archerows;

/**
 * Represents the most basic SQL task for ArcheCore. Inserts will be run as-is assuming that the declaration is not of {@link net.lordofthecraft.arche.save.archerows.ArchePreparedStatementRow}
 *
 * @author 501warhead
 */
public interface ArcheRow {

    /**
     * honestly not sure yet
     *
     * @return TBD
     */
    String[] getInserts();
}
