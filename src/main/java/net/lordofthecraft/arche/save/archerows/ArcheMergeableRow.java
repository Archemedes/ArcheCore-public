package net.lordofthecraft.arche.save.archerows;

import java.sql.PreparedStatement;

/**
 * This is a high level concept, be warned and this will be something that if you do not understand you should simply use {@link net.lordofthecraft.arche.save.archerows.ArchePreparedStatementRow}
 * <p>
 * An ArcheMergeableRow is an SQL object which can be effectively merged to be run collectively as a batch ( {@link PreparedStatement#addBatch()} & {@link PreparedStatement#executeBatch()} )
 * <p>
 * In a loose example you can see {@link net.lordofthecraft.arche.save.archerows.persona.update.MultiPersonaUpdateRow} and {@link net.lordofthecraft.arche.save.archerows.persona.update.PersonaUpdateRow}
 * <p>
 * MergeableRows usually have {@link java.sql.PreparedStatement}s which are the same, and can be used for multiple entries.
 * For example, say I am having the {@link net.lordofthecraft.arche.persona.ArchePersona} named George updated so he has 20 health.
 * Then, I have the {@link net.lordofthecraft.arche.persona.ArchePersona} named Bob have his health set to 4.
 * <p>
 * For both, the {@link java.sql.PreparedStatement} might look something like "UPDATE persona SET health=? WHERE persona_id=?"
 * In this instance, I can have both "merge" together.
 * First, we use this prepared statement to set George's health to 20, then we set Bob's health to 4, adding each to our Batch which allows them to be executed at the same time.
 *
 * @author 501warhead
 */
public interface ArcheMergeableRow extends ArchePreparedStatementRow {

    /**
     * Checks whether or not this row is "Unique" and therefore cannot be merged with another row regardless of state.
     * @return Whether or not this is unique
     */
    boolean isUnique();

    /**
     * Check whether or not this row can be merged with another.
     * @param row The other row to check if it can merge
     * @return Whether or not this is a valid merge.
     */
    boolean canMerge(ArcheMergeableRow row);

    /**
     * Merge the rows together if {@link #canMerge(ArcheMergeableRow)} succeeds.
     *
     * @param second The row to merge
     * @return The combined rows
     */
    ArcheMergeableRow merge(ArcheMergeableRow second);
}
